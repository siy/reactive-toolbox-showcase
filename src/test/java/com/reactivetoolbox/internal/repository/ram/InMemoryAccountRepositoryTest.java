package com.reactivetoolbox.internal.repository.ram;

import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.User;
import com.reactivetoolbox.internal.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivetoolbox.core.async.Promise;
import org.reactivetoolbox.core.lang.support.ClockMock;
import org.reactivetoolbox.core.meta.AppMetaRepository;
import org.reactivetoolbox.core.scheduler.Timeout;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.reactivetoolbox.domain.SupportedCurrencies.EUR;
import static com.reactivetoolbox.domain.SupportedCurrencies.GBP;
import static com.reactivetoolbox.domain.SupportedCurrencies.USD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.reactivetoolbox.core.async.Promise.RethrowingCollector.collector;
import static org.reactivetoolbox.core.lang.Tuple.tuple;

class InMemoryAccountRepositoryTest {
    private static final Timeout _1_SECOND = Timeout.timeout(1).seconds();

    private final AccountRepository repository = new InMemoryAccountRepository();

    private final User.Id userId1 = User.create("Johnny", "Rico").id();
    private final User.Id userId2 = User.create("Ace", "Levy").id();
    private final User.Id userId3 = User.create("Jean", "Rasczak").id();

    private final Account account1 = Account.createEmpty(userId1, EUR);
    private final Account account2 = Account.createEmpty(userId2, USD);
    private final Account account3 = Account.createEmpty(userId3, GBP);

    @BeforeEach
    void setUp() {
        Promise.all(repository.save(account1),
                    repository.save(account2),
                    repository.save(account3))
               .onFailure($ -> fail())
               .syncWait(_1_SECOND);
    }

    @Test
    void existingAccountsCanLoaded() {
        final var collector = collector();

        Promise.all(repository.find(account1.id()),
                    repository.find(account2.id()),
                    repository.find(account3.id()))
               .exceptionCollector(collector)
               .onSuccess(accounts -> assertEquals(tuple(account1, account2, account3), accounts))
               .onFailure($ -> fail())
               .syncWait(_1_SECOND);

        collector.rethrow();
    }

    @Test
    void newAccountCanBeSavedAndLoaded() {
        final var collector = collector();
        final var account = Account.createEmpty(userId1, GBP);

        repository.save(account)
                  .exceptionCollector(collector)
                  .onSuccess(acc -> assertEquals(account, acc))
                  .onFailure($ -> fail())
                  .chainMap($ -> repository.find(account.id())
                                           .onSuccess(acc -> assertEquals(account, acc))
                                           .onFailure($$ -> fail()))
                  .syncWait(_1_SECOND);

        collector.rethrow();
    }

    @Test
    void missingAccountIsReported() {
        final var collector = collector();
        final var nonExistent = Account.createEmpty(userId2, GBP);

        repository.find(nonExistent.id())
                  .exceptionCollector(collector)
                  .onSuccess($ -> fail())
                  .syncWait(_1_SECOND);

        collector.rethrow();
    }
}