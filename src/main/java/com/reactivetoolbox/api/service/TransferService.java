package com.reactivetoolbox.api.service;

import com.reactivetoolbox.api.request.TransferRequest;
import com.reactivetoolbox.api.response.TransferResponse;
import org.reactivetoolbox.core.async.Promise;

public interface TransferService {
    Promise<TransferResponse> transfer(final TransferRequest request);
}
