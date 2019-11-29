package com.reactivetoolbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactivetoolbox.api.request.AccountCreateRequest;
import com.reactivetoolbox.api.request.HistoryRequest;
import com.reactivetoolbox.api.request.TransferRequest;
import com.reactivetoolbox.api.response.AccountCreateResponse;
import com.reactivetoolbox.api.response.AccountResponse;
import com.reactivetoolbox.api.response.HistoryResponse;
import com.reactivetoolbox.api.response.Status;
import com.reactivetoolbox.api.response.TransferResponse;
import com.reactivetoolbox.api.service.AccountService;
import com.reactivetoolbox.api.service.HistoryService;
import com.reactivetoolbox.api.service.TransferService;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.Slf4jLogger;
import com.reactivetoolbox.internal.codec.AppModule;
import com.reactivetoolbox.internal.repository.AccountRepository;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import com.reactivetoolbox.internal.repository.ram.InMemoryAccountRepository;
import com.reactivetoolbox.internal.repository.ram.InMemoryHistoryRepository;
import com.reactivetoolbox.internal.services.SimpleAccountService;
import com.reactivetoolbox.internal.services.SimpleHistoryService;
import com.reactivetoolbox.internal.services.SimpleTransferService;
import io.jooby.Context;
import io.jooby.Extension;
import io.jooby.json.JacksonModule;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.Functions.FN1;
import org.reactivetoolbox.core.lang.Result;
import org.reactivetoolbox.core.lang.support.KSUID;
import org.reactivetoolbox.core.log.CoreLogger;
import org.reactivetoolbox.core.meta.AppMetaRepository;
import org.reactivetoolbox.core.scheduler.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.reactivetoolbox.api.request.TransferRequest.transferRequest;
import static com.reactivetoolbox.domain.Failures.invalidParameter;
import static com.reactivetoolbox.domain.Operation.split;
import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.Transfer.from;
import static io.jooby.Jooby.runApp;
import static org.reactivetoolbox.core.lang.Option.option;
import static org.reactivetoolbox.core.lang.Range.range;
import static org.reactivetoolbox.core.lang.ThrowingFunctions.wrap;
import static org.reactivetoolbox.core.lang.Tuple.tuple;

public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final Timeout _1_SECOND = Timeout.timeout(1).seconds();

    private final AccountService accountService;
    private final HistoryService historyService;
    private final TransferService transferService;
    private final ObjectMapper objectMapper;

    private Application() {
        final AccountRepository accountRepository = new InMemoryAccountRepository();
        final HistoryRepository historyRepository = new InMemoryHistoryRepository();

        accountService = new SimpleAccountService(accountRepository);
        historyService = new SimpleHistoryService(historyRepository);
        transferService = new SimpleTransferService(historyRepository, accountRepository);
        objectMapper = configureMapper();

        configureDemoAccounts(accountRepository, historyRepository);
    }

    public static void main(String[] args) {
        AppMetaRepository.instance().put(CoreLogger.class, new Slf4jLogger());

        final var application = new Application();

        runApp(args, app -> {
            app.install(application.getModule());

            app.get("/healthcheck", context -> CompletableFuture.supplyAsync(Status::success));

            app.path("/v1/api", () -> {
                app.path("/user", () -> {
                    //User API is not implemented
                    //app.post("/user", application::createUser);
                    //app.get("/user/{id}", application::getUser);
                });

                app.path("/account", () -> {
                    app.post("/", application::createAccount);
                    app.get("/{accountId}", application::getAccount);
                    app.get("/{accountId}/history", application::getAccountHistory);
                });

                app.post("/transfer", application::createTransfer);
            });
        });
    }

    private Application configureDemoAccounts(final AccountRepository accountRepository,
                                              final HistoryRepository historyRepository) {
        final var userId1 = User.create("Johnny", "Rico");
        final var userId2 = User.create("Ace", "Levy");

        final var userId0 = User.create("The", "Bank");
        final var bankAccount = Account.createEmpty(userId0.id(), EUR);

        split(from(transferRequest(bankAccount.id(),
                                   bankAccount.id(),
                                   BigDecimal.valueOf(1e6), EUR)))
                .map((op1, op2) ->
                             bankAccount.apply(op2)
                                        .onSuccess(tuple ->
                                                           tuple.map((account, operation) ->
                                                                             Promise.all(accountRepository.save(account),
                                                                                         historyRepository.save(operation))
                                                                                    .onSuccess($ -> LOG.info("Created account for Bank: {}", account))
                                                                                    .syncWait(_1_SECOND))));
        accountRepository.save(Account.createEmpty(userId1.id(), EUR))
                         .onSuccess(account -> LOG.info("Created account {} for User {}", account, userId1))
                         .syncWait(_1_SECOND);
        accountRepository.save(Account.createEmpty(userId2.id(), EUR))
                         .onSuccess(account -> LOG.info("Created account {} for User {}", account, userId2))
                         .syncWait(_1_SECOND);

        return this;
    }

    private ObjectMapper configureMapper() {
        return JacksonModule.create().registerModule(new AppModule());
    }

    private Extension getModule() {
        return new JacksonModule(objectMapper);
    }

    private Result<Account.Id> extractAccountId(final Context context, final String name) {
        return option(context.path(name).value())
                       .map(KSUID::fromString)
                       .map(id -> id.map(Account.Id::with))
                       .otherwise(invalidParameter(name).asResult());
    }

    private Result<ZonedDateTime> extractDate(final Context context, final String name) {
        return option(context.query(name).value())
                       .map(value -> wrap(() -> ZonedDateTime.parse(value)))
                       .otherwise(invalidParameter(name).asResult());
    }

    private <T> Consumer<Failure> failureHandler(final CompletableFuture<T> future, final FN1<T, Failure> converter) {
        return failure -> future.complete(converter.apply(failure));
    }

    private <T, R> Consumer<R> successHandler(final CompletableFuture<T> future, final FN1<T, R> converter) {
        return result -> future.complete(converter.apply(result));
    }

    private CompletableFuture<HistoryResponse> getAccountHistory(final Context context) {
        final var future = new CompletableFuture<HistoryResponse>();
        final var failureHandler = failureHandler(future, HistoryResponse::failure);

        tuple(extractAccountId(context, "accountId"),
              extractDate(context, "from"),
              extractDate(context, "to"))
                .map(Result::zip)
                .map(tuple -> tuple.map((accountId, dateFrom, dateTo) -> HistoryRequest.request(accountId, range(dateFrom, dateTo))))
                .map(historyService::history)
                .onFailure(failureHandler)
                .onSuccess(promise -> promise.onSuccess(future::complete)
                                             .onFailure(failureHandler));
        return future;
    }

    private CompletableFuture<AccountCreateResponse> createAccount(final Context context) {
        final var future = new CompletableFuture<AccountCreateResponse>();
        final var failureHandler = failureHandler(future, AccountCreateResponse::failure);

        option(context.body(AccountCreateRequest.class))
                .map(accountService::create)
                .otherwise(Promise.readyFail(invalidParameter("accountCreateRequest")))
                .onFailure(failureHandler)
                .onSuccess(future::complete);

        return future;
    }

    private CompletableFuture<AccountResponse> getAccount(final Context context) {
        final var future = new CompletableFuture<AccountResponse>();
        final var failureHandler = failureHandler(future, AccountResponse::failure);
        final var successHandler = successHandler(future, AccountResponse::success);

        extractAccountId(context, "accountId")
                .onFailure(failureHandler)
                .onSuccess(accountId -> accountService.find(accountId)
                                                      .onFailure(failureHandler)
                                                      .onSuccess(successHandler));

        return future;
    }

    private CompletableFuture<TransferResponse> createTransfer(final Context context) {
        final var future = new CompletableFuture<TransferResponse>();
        final var failureHandler = failureHandler(future, TransferResponse::failure);

        option(context.body(TransferRequest.class))
                .map(transferService::transfer)
                .otherwise(Promise.readyFail(invalidParameter("transferRequest")))
                .onFailure(failureHandler)
                .onSuccess(future::complete);

        return future;
    }
}
