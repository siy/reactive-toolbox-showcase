package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.request.HistoryRequest;
import com.reactivetoolbox.api.response.HistoryResponse;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Currency;
import com.reactivetoolbox.domain.Operation;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.junit.jupiter.api.Test;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Range;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;
import org.reactivetoolbox.core.lang.support.ClockMock;
import org.reactivetoolbox.core.meta.AppMetaRepository;
import org.reactivetoolbox.core.scheduler.Timeout;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.reactivetoolbox.api.request.TransferRequest.transferRequest;
import static com.reactivetoolbox.domain.Operation.split;
import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.Transfer.from;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.reactivetoolbox.core.async.Promise.RethrowingCollector.collector;
import static org.reactivetoolbox.core.lang.List.list;
import static org.reactivetoolbox.core.lang.Range.range;
import static org.reactivetoolbox.core.scheduler.Timeout.timeout;

class SimpleHistoryServiceTest {
    private final HistoryRepository repository = mock(HistoryRepository.class);
    private final SimpleHistoryService service = new SimpleHistoryService(repository);

    private final User.Id userId1 = User.create("Johnny", "Rico").id();
    private final User.Id userId2 = User.create("Ace", "Levy").id();

    private final Account account1 = Account.createEmpty(userId1, EUR);
    private final Account account2 = Account.createEmpty(userId2, EUR);

    private final HistoryResponse historyResponse = HistoryResponse.success(with(account1.id(), account2.id(), BigDecimal.valueOf(123.15), EUR)
                                                                            .map((op1, op2) -> list(op1, op2)));
    private final Range<LocalDateTime> searchRange = range(LocalDateTime.of(2019, 11, 27, 0, 0),
                                                           LocalDateTime.of(2019, 11, 28, 0, 0));
    private final HistoryRequest request = HistoryRequest.request(account1.id(),
                                                                  searchRange.map(date -> ZonedDateTime.of(date, ZoneOffset.UTC)));

    static {
        AppMetaRepository.instance()
                         .put(Clock.class,
                              ClockMock.with(LocalDateTime.of(2019, 11, 27, 18, 0),
                                             timeout(1).seconds()));
    }

    @Test
    void historySearchIsPassedToRepository() {
        final var collector = collector();

        when(repository.retrieve(account1.id(), searchRange))
                .thenReturn(Promise.readyOk(historyResponse));

        service.history(request)
               .exceptionCollector(collector)
               .onSuccess(response -> assertEquals(historyResponse, response))
               .onFailure($ -> fail())
               .syncWait(Timeout.timeout(1).seconds());

        verify(repository).retrieve(account1.id(), searchRange);

        collector.rethrow();
    }

    private Tuple2<Operation, Operation> with(final Account.Id fromAccount,
                                              final Account.Id toAccount,
                                              final BigDecimal amount,
                                              final Currency currency) {

        return split(from(transferRequest(fromAccount, toAccount, amount, currency)));
    }
}