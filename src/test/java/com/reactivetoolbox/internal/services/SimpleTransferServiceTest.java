package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.service.TransferService;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Currency;
import com.reactivetoolbox.domain.Operation;
import com.reactivetoolbox.domain.Transfer;
import com.reactivetoolbox.domain.TransferFailureTypes;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.repository.AccountRepository;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.List;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;
import org.reactivetoolbox.core.lang.support.CollectionBuilder;
import org.reactivetoolbox.core.lang.support.WebFailureTypes;

import java.math.BigDecimal;

import static com.reactivetoolbox.api.request.TransferRequest.transferRequest;
import static com.reactivetoolbox.domain.Operation.split;
import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.SupportedCurrencies.GBP;
import static com.reactivetoolbox.domain.Transfer.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
import static org.reactivetoolbox.core.async.Promise.RethrowingCollector.collector;
import static org.reactivetoolbox.core.lang.Option.option;
import static org.reactivetoolbox.core.scheduler.Timeout.timeout;

class SimpleTransferServiceTest {
    private final HistoryRepository historyRepository = mock(HistoryRepository.class);
    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final TransferService service = new SimpleTransferService(historyRepository, accountRepository);
    private final User.Id userId0 = User.create("The", "Bank").id();

    private final User.Id userId1 = User.create("Johnny", "Rico").id();
    private final User.Id userId2 = User.create("Ace", "Levy").id();

    private final Account account0 = initBankAccount();

    private Account initBankAccount() {
        final var account = Account.createEmpty(userId0, EUR);
        final var initialTransfer = with(account.id(), account.id(), BigDecimal.valueOf(1000.0), EUR)
                                            .map((op1, op2) -> op2);

        return account.apply(initialTransfer).map($ -> {
                                                      fail();
                                                      return null;
                                                  },
                                                  tuple -> tuple.map((acc, op) -> acc));
    }

    private final Account account1 = Account.createEmpty(userId1, EUR);
    private final Account account2 = Account.createEmpty(userId2, EUR);
    private final Account account3 = Account.createEmpty(userId1, GBP);

    private final Failure customFailure = Failure.failure(WebFailureTypes.INTERNAL_SERVER_ERROR, "Server failure");

    @Test
    void transferCanBePerformed() {
        final var collector = collector();
        final var operationRecorder = Recorder.recorder(Operation.class);
        final var accountRecorder = Recorder.recorder(Account.class);

        final var request = transferRequest(account0.id(),
                                            account1.id(),
                                            BigDecimal.valueOf(234.56),
                                            EUR);

        when(accountRepository.find(account0.id())).thenReturn(Promise.readyOk(account0));
        when(accountRepository.find(account1.id())).thenReturn(Promise.readyOk(account1));

        when(historyRepository.save(any(Operation.class))).thenAnswer(operationRecorder);
        when(accountRepository.save(any(Account.class))).thenAnswer(accountRecorder);

        service.transfer(request)
               .exceptionCollector(collector)
               .onSuccess(response -> assertEquals(0, response.status().code()))
               .onFailure($ -> fail())
               .syncWait(timeout(1).seconds());

        verify(accountRepository).find(account0.id());
        verify(accountRepository).find(account1.id());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(historyRepository, times(2)).save(any(Operation.class));

        final List<Operation> operations = operationRecorder.collected();
        final List<Account> accounts = accountRecorder.collected();

        assertEquals(2, operations.size());

        final Transfer.Id transferId1 = operations.first()
                                                  .map(Operation::transferId)
                                                  .otherwise(null);

        final Transfer.Id transferId2 = operations.last()
                                                  .map(Operation::transferId)
                                                  .otherwise(null);

        assertEquals(transferId1, transferId2);

        //Operation on source account
        operations.first()
                  .whenEmpty(Assertions::fail)
                  .whenPresent(operation -> assertEquals(BigDecimal.valueOf(-234.56), operation.amount()))
                  .whenPresent(operation -> assertEquals(option(BigDecimal.valueOf(765.44)), operation.balance()))
                  .whenPresent(operation -> assertEquals(account0.id(), operation.local()))
                  .whenPresent(operation -> assertEquals(account1.id(), operation.remote()));
        //Operation on destination account
        operations.last()
                  .whenEmpty(Assertions::fail)
                  .whenPresent(operation -> assertEquals(BigDecimal.valueOf(234.56), operation.amount()))
                  .whenPresent(operation -> assertEquals(option(BigDecimal.valueOf(234.56)), operation.balance()))
                  .whenPresent(operation -> assertEquals(account1.id(), operation.local()))
                  .whenPresent(operation -> assertEquals(account0.id(), operation.remote()));

        assertEquals(2, accounts.size());

        accounts.first()
                .whenEmpty(Assertions::fail)
                .whenPresent(account -> assertEquals(account0.id(), account.id()))
                .whenPresent(account -> assertEquals(EUR, account.currency()))
                .whenPresent(account -> assertEquals(userId0, account.userId()))
                .whenPresent(account -> assertEquals(BigDecimal.valueOf(765.44), account.amount()))
                .whenPresent(account -> assertEquals(option(transferId1), account.lastTransfer()));

        accounts.last()
                .whenEmpty(Assertions::fail)
                .whenPresent(account -> assertEquals(account1.id(), account.id()))
                .whenPresent(account -> assertEquals(EUR, account.currency()))
                .whenPresent(account -> assertEquals(userId1, account.userId()))
                .whenPresent(account -> assertEquals(BigDecimal.valueOf(234.56), account.amount()))
                .whenPresent(account -> assertEquals(option(transferId1), account.lastTransfer()));

        collector.rethrow();
    }

    @Test
    void transferFailsIfNoSufficientFunds() {
        final var collector = collector();

        final var request = transferRequest(account1.id(),
                                            account2.id(),
                                            BigDecimal.valueOf(1.00),
                                            EUR);

        when(accountRepository.find(account1.id())).thenReturn(Promise.readyOk(account1));
        when(accountRepository.find(account2.id())).thenReturn(Promise.readyOk(account2));

        when(historyRepository.save(any(Operation.class))).thenAnswer(answerPromise(Operation.class));
        when(accountRepository.save(any(Account.class))).thenAnswer(answerPromise(Account.class));

        service.transfer(request)
               .exceptionCollector(collector)
               .onSuccess(response -> fail())
               .onFailure(failure -> assertEquals(TransferFailureTypes.INSUFFICIENT_FUNDS, failure.type()))
               .syncWait(timeout(1).seconds());

        verify(accountRepository).find(account1.id());
        verify(accountRepository).find(account2.id());
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(historyRepository, times(0)).save(any(Operation.class));

        collector.rethrow();
    }

    @Test
    void transferFailsIfAccountCurrencyDoesNotMatch() {
        final var collector = collector();

        final var request = transferRequest(account0.id(),
                                            account3.id(),
                                            BigDecimal.valueOf(1.00),
                                            EUR);

        when(accountRepository.find(account0.id())).thenReturn(Promise.readyOk(account0));
        when(accountRepository.find(account3.id())).thenReturn(Promise.readyOk(account3));

        when(historyRepository.save(any(Operation.class))).thenAnswer(answerPromise(Operation.class));
        when(accountRepository.save(any(Account.class))).thenAnswer(answerPromise(Account.class));

        service.transfer(request)
               .exceptionCollector(collector)
               .onSuccess(response -> fail())
               .onFailure(failure -> assertEquals(TransferFailureTypes.CURRENCY_MISMATCH, failure.type()))
               .syncWait(timeout(1).seconds());

        verify(accountRepository).find(account0.id());
        verify(accountRepository).find(account3.id());
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(historyRepository, times(0)).save(any(Operation.class));

        collector.rethrow();
    }

    @Test
    void transferFailsIfAccountSavingFails() {
        final var collector = collector();

        final var request = transferRequest(account0.id(),
                                            account1.id(),
                                            BigDecimal.valueOf(1.00),
                                            EUR);

        when(accountRepository.find(account0.id())).thenReturn(Promise.readyOk(account0));
        when(accountRepository.find(account1.id())).thenReturn(Promise.readyOk(account1));

        when(historyRepository.save(any(Operation.class))).thenAnswer(answerPromise(Operation.class));
        when(accountRepository.save(any(Account.class))).thenReturn(Promise.readyFail(customFailure));

        service.transfer(request)
               .exceptionCollector(collector)
               .onSuccess(response -> fail())
               .onFailure(failure -> assertEquals(customFailure, failure))
               .syncWait(timeout(1).seconds());

        verify(accountRepository).find(account0.id());
        verify(accountRepository).find(account1.id());
        verify(historyRepository, times(2)).save(any(Operation.class));
        verify(accountRepository, times(2)).save(any(Account.class));

        collector.rethrow();
    }

    @Test
    void transferFailsIfOperationSavingFails() {
        final var collector = collector();

        final var request = transferRequest(account0.id(),
                                            account1.id(),
                                            BigDecimal.valueOf(1.00),
                                            EUR);

        when(accountRepository.find(account0.id())).thenReturn(Promise.readyOk(account0));
        when(accountRepository.find(account1.id())).thenReturn(Promise.readyOk(account1));

        when(historyRepository.save(any(Operation.class))).thenReturn(Promise.readyFail(customFailure));
        when(accountRepository.save(any(Account.class))).thenAnswer(answerPromise(Account.class));

        service.transfer(request)
               .exceptionCollector(collector)
               .onSuccess(response -> fail())
               .onFailure(failure -> assertEquals(customFailure, failure))
               .syncWait(timeout(1).seconds());

        verify(accountRepository).find(account0.id());
        verify(accountRepository).find(account1.id());
        verify(historyRepository, times(2)).save(any(Operation.class));
        verify(accountRepository, times(2)).save(any(Account.class));

        collector.rethrow();
    }

    private static <T> Answer<Promise<T>> answerPromise(final Class<T> type) {
        return invocation -> Promise.readyOk(invocation.getArgument(0, type));
    }

    private static class Recorder<T> implements Answer<Promise<T>> {
        private final Class<T> type;
        private final CollectionBuilder<List<T>, T> builder = List.builder();

        private Recorder(final Class<T> type) {
            this.type = type;
        }

        @Override
        public Promise<T> answer(final InvocationOnMock invocation) throws Throwable {
            final T value = invocation.getArgument(0, type);
            builder.append(value);
            return Promise.readyOk(value);
        }

        public List<T> collected() {
            return builder.build();
        }

        public static <T> Recorder<T> recorder(final Class<T> type) {
            return new Recorder<>(type);
        }
    }

    private Tuple2<Operation, Operation> with(final Account.Id fromAccount,
                                              final Account.Id toAccount,
                                              final BigDecimal amount,
                                              final Currency currency) {
        return split(from(transferRequest(fromAccount, toAccount, amount, currency)));
    }
}