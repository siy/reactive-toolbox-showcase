package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.request.HistoryRequest;
import com.reactivetoolbox.api.response.HistoryResponse;
import com.reactivetoolbox.api.service.HistoryService;
import com.reactivetoolbox.internal.repository.HistoryRepository;
import org.reactivetoolbox.core.async.Promise;

import java.time.ZoneOffset;

public class SimpleHistoryService implements HistoryService {
    private final HistoryRepository repository;

    public SimpleHistoryService(final HistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Promise<HistoryResponse> history(final HistoryRequest request) {
        return repository.retrieve(request.accountId(), request.range()
                                                               .map(bound -> bound.withZoneSameInstant(ZoneOffset.UTC)
                                                                                  .toLocalDateTime()));
    }
}
