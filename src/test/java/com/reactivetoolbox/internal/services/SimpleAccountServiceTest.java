package com.reactivetoolbox.internal.services;

import com.reactivetoolbox.api.request.AccountCreateRequest;
import com.reactivetoolbox.api.response.Status;
import com.reactivetoolbox.api.service.AccountService;
import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.Option;

import static com.reactivetoolbox.api.request.AccountCreateRequest.*;
import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.SupportedCurrencies.GBP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.reactivetoolbox.core.async.Promise.RethrowingCollector.collector;

class SimpleAccountServiceTest {
    private final AccountRepository repository = mock(AccountRepository.class);
    private final AccountService service = new SimpleAccountService(repository);

    private final User.Id userId1 = User.create("Johnny", "Rico").id();
    private final User.Id userId2 = User.create("Ace", "Levy").id();

    private final Account account1 = Account.createEmpty(userId1, EUR);

    @Test
    void accountSearchIsPassedToRepository() {
        final var collector = collector();

        when(repository.find(account1.id()))
                .thenReturn(Promise.readyOk(account1));

        service.find(account1.id())
               .exceptionCollector(collector)
               .onSuccess(account -> assertEquals(account1, account))
               .onFailure($ -> fail());

        verify(repository).find(account1.id());

        collector.rethrow();
    }

    @Test
    void accountCanBeCreated() {
        final var collector = collector();
        final var newAccount = Account.createEmpty(userId2, GBP);

        when(repository.save(any(Account.class)))
                .thenReturn(Promise.readyOk(newAccount));

        service.create(request(userId2, GBP))
               .exceptionCollector(collector)
               .onSuccess(response -> assertEquals(Status.success(), response.status()))
               .onSuccess(response -> assertEquals(Option.option(newAccount.id()), response.accountId()))
               .onFailure($ -> fail());

        verify(repository).save(any(Account.class));

        collector.rethrow();
    }
}