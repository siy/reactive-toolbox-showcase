package com.reactivetoolbox.internal.repository.ram;

import com.reactivetoolbox.api.response.HistoryResponse;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Operation;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.List;
import org.reactivetoolbox.core.lang.Range;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryHistoryRepository implements HistoryRepository {
    private final java.util.Queue<Operation> storage = new ConcurrentLinkedQueue<>();

    @Override
    public Promise<Operation> save(final Operation operation) {
        return Promise.<Operation>promise()
                       .async(promise -> {
                           storage.add(operation);
                           promise.asyncOk(operation);
                       });
    }

    @Override
    public Promise<HistoryResponse> retrieve(final Account.Id accountId, final Range<LocalDateTime> range) {
        return Promise.<HistoryResponse>promise()
                       .async(promise -> collectHistory(promise, accountId, range));
    }

    private void collectHistory(final Promise<HistoryResponse> promise,
                                final Account.Id accountId,
                                final Range<LocalDateTime> range) {
        final var operations = storage.stream()
                                      .filter(op -> op.local().equals(accountId))
                                      .filter(op -> op.timestamp().isAfter(range.from()))
                                      .filter(op -> op.timestamp().isBefore(range.to()))
                                      .collect(List.toList());
        promise.asyncOk(HistoryResponse.success(operations));
    }
}
