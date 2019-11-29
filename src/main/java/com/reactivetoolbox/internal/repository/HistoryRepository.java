package com.reactivetoolbox.internal.repository;

import com.reactivetoolbox.api.response.HistoryResponse;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Operation;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Range;

import java.time.LocalDateTime;

public interface HistoryRepository {
    Promise<Operation> save(final Operation operation);

    Promise<HistoryResponse> retrieve(Account.Id accountId, Range<LocalDateTime> range);
}
