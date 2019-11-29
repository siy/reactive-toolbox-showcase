package com.reactivetoolbox.api.service;

import com.reactivetoolbox.api.request.AccountCreateRequest;
import com.reactivetoolbox.api.response.AccountCreateResponse;
import com.reactivetoolbox.domain.Account;
import org.reactivetoolbox.core.async.Promise;

public interface AccountService {
    Promise<AccountCreateResponse> create(final AccountCreateRequest request);

    Promise<Account> find(final Account.Id accountId);
}
