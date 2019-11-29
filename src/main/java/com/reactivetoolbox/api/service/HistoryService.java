package com.reactivetoolbox.api.service;

import com.reactivetoolbox.api.request.HistoryRequest;
import com.reactivetoolbox.api.response.HistoryResponse;
import org.reactivetoolbox.core.async.Promise;

public interface HistoryService {
    Promise<HistoryResponse> history(final HistoryRequest request);
}
