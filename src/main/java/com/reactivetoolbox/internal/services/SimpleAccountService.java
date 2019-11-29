package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.request.AccountCreateRequest;
import com.reactivetoolbox.api.response.AccountCreateResponse;
import com.reactivetoolbox.api.service.AccountService;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.internal.repository.AccountRepository;
import org.reactivetoolbox.core.async.Promise;

import static com.reactivetoolbox.api.response.AccountCreateResponse.success;

public class SimpleAccountService implements AccountService {
    private final AccountRepository repository;

    public SimpleAccountService(final AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Promise<AccountCreateResponse> create(final AccountCreateRequest request) {
        return repository.save(Account.createEmpty(request.userId(), request.currency()))
                         .map(account -> success(account.id()));
    }

    @Override
    public Promise<Account> find(final Account.Id accountId) {
        return repository.find(accountId);
    }
}
