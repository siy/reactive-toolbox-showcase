package com.reactivetoolbox.internal.repository.ram;

import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.internal.repository.AccountRepository;
import org.reactivetoolbox.core.async.Promise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.reactivetoolbox.domain.Failures.accountNotFound;
import static org.reactivetoolbox.core.lang.Option.option;

public class InMemoryAccountRepository implements AccountRepository {
    private static final ConcurrentMap<Account.Id, Account> storage = new ConcurrentHashMap<>();

    @Override
    public Promise<Account> save(final Account account) {
        return Promise.<Account>promise()
                       .async(promise -> {
                           storage.put(account.id(), account);
                           promise.asyncOk(account);
                       });
    }

    @Override
    public Promise<Account> find(final Account.Id accountId) {
        return Promise.<Account>promise()
                       .async(promise -> option(storage.get(accountId))
                                                 .map($ -> promise.fail(accountNotFound(accountId)),
                                                      promise::asyncOk));
    }
}
