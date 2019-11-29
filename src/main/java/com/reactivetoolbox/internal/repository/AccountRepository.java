package com.reactivetoolbox.internal.repository;

import com.reactivetoolbox.domain.Account;
import org.reactivetoolbox.core.async.Promise;

public interface AccountRepository {
    Promise<Account> save(final Account account);

    Promise<Account> find(final Account.Id accountId);
}
