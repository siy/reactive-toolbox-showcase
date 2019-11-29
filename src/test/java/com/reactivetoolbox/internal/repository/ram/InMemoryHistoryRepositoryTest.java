package com.reactivetoolbox.internal.repository.ram;

import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Currency;
import com.reactivetoolbox.domain.Operation;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.junit.jupiter.api.Test;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;
import org.reactivetoolbox.core.lang.support.ClockMock;
import org.reactivetoolbox.core.meta.AppMetaRepository;
import org.reactivetoolbox.core.scheduler.Timeout;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static com.reactivetoolbox.api.request.TransferRequest.transferRequest;
import static com.reactivetoolbox.domain.Operation.split;
import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.Transfer.from;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.reactivetoolbox.core.async.Promise.RethrowingCollector.collector;
import static org.reactivetoolbox.core.lang.Option.option;
import static org.reactivetoolbox.core.lang.Range.range;
import static org.reactivetoolbox.core.lang.Tuple.tuple;
import static org.reactivetoolbox.core.scheduler.Timeout.timeout;

class InMemoryHistoryRepositoryTest {
    private static final Timeout _1_SECOND = Timeout.timeout(1).seconds();
    private final HistoryRepository historyRepository = new InMemoryHistoryRepository();

    private final User.Id userId1 = User.create("Johnny", "Rico").id();
    private final User.Id userId2 = User.create("Ace", "Levy").id();
    private final User.Id userId3 = User.create("Jean", "Rasczak").id();

    private final Account account1 = Account.createEmpty(userId1, EUR);
    private final Account account2 = Account.createEmpty(userId2, EUR);

    private final Tuple2<Operation, Operation> transfer1 = with(account1.id(), account2.id(), BigDecimal.valueOf(123.15), EUR);
    private final Operation operation1 = transfer1.map((op1, op2) -> op1);
    private final Operation operation2 = transfer1.map((op1, op2) -> op2);

    static {
        AppMetaRepository.instance()
                         .put(Clock.class,
                              ClockMock.with(LocalDateTime.of(2019, 11, 27, 18, 0),
                                             timeout(1).seconds()));
    }

    @Test
    void historyCanBeSaved() {
        final var collector = collector();

        historyRepository.save(operation1)
                         .exceptionCollector(collector)
                         .onSuccess(op1 -> assertEquals(operation1, op1))
                         .onFailure($ -> fail())
                         .syncWait(_1_SECOND);

        collector.rethrow();
    }

    @Test
    void historyCanBeRetrieved() {
        final var collector = collector();
        final var today = LocalDateTime.of(2019, 11, 27, 0, 0);
        final var tomorrow = LocalDateTime.of(2019, 11, 28, 0, 0);

        Promise.all(historyRepository.save(operation1),
                    historyRepository.save(operation2))
               .exceptionCollector(collector)
               .onSuccess(tuple -> assertEquals(tuple(operation1, operation2), tuple))
               .onFailure($ -> fail())
               .syncWait(_1_SECOND);

        historyRepository.retrieve(operation1.local(), range(today, tomorrow))
                         .exceptionCollector(collector)
                         .onSuccess(response -> assertEquals(option(operation1), response.operations().first()))
                         .onFailure($ -> fail())
                         .syncWait(_1_SECOND);

        historyRepository.retrieve(operation2.local(), range(today, tomorrow))
                         .exceptionCollector(collector)
                         .onSuccess(response -> assertEquals(option(operation2), response.operations().first()))
                         .onFailure($ -> fail())
                         .syncWait(_1_SECOND);

        collector.rethrow();
    }

    private Tuple2<Operation, Operation> with(final Account.Id fromAccount,
                                              final Account.Id toAccount,
                                              final BigDecimal amount,
                                              final Currency currency) {
        return split(from(transferRequest(fromAccount, toAccount, amount, currency)));
    }
}