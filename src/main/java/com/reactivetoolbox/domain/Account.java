package com.reactivetoolbox.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.reactivetoolbox.core.lang.Option;
import org.reactivetoolbox.core.lang.Result;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;
import org.reactivetoolbox.core.lang.support.KSUID;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

import static org.reactivetoolbox.core.lang.Option.empty;
import static org.reactivetoolbox.core.lang.Option.option;
import static org.reactivetoolbox.core.lang.Tuple.tuple;

public class Account {
    private final Id id;
    private final User.Id userId;
    private final Currency currency;
    private final BigDecimal amount;
    private final Option<Transfer.Id> lastTransfer;

    private Account(final Id id,
                    final User.Id userId,
                    final Currency currency,
                    final BigDecimal amount,
                    final Option<Transfer.Id> lastTransfer) {
        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.amount = amount;
        this.lastTransfer = lastTransfer;
    }

    @JsonGetter("id")
    public Id id() {
        return id;
    }

    @JsonGetter("userId")
    public User.Id userId() {
        return userId;
    }

    @JsonGetter("currency")
    public Currency currency() {
        return currency;
    }

    @JsonGetter("amount")
    public BigDecimal amount() {
        return amount;
    }

    @JsonGetter("lastTransactionId")
    public Option<Transfer.Id> lastTransfer() {
        return lastTransfer;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }
        final Account account = (Account) o;

        return id.equals(account.id) &&
               userId.equals(account.userId) &&
               currency.equals(account.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, currency);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Account(", ")")
                       .add("account" + id)
                       .add("user" + userId)
                       .add(amount.toString() + " " + currency.id())
                       .add(lastTransfer.toString())
                       .toString();
    }

    public Result<Tuple2<Account, Operation>> apply(final Operation operation) {
        if (!operation.currency().equals(currency)) {
            return Failures.currencyDoesNotMatch(operation.currency(), currency);
        }

        final BigDecimal resultAmount = this.amount.add(operation.amount());

        if (resultAmount.compareTo(BigDecimal.ZERO) < 0) {
            return Failures.insufficientFunds(id);
        }

        return Result.ok(tuple(updateAmountForOperation(resultAmount, operation.transferId()),
                               operation.chain(lastTransfer, resultAmount)));
    }

    private Account updateAmountForOperation(final BigDecimal amount, final Transfer.Id transferId) {
        return new Account(id, userId, currency, amount, option(transferId));
    }

    public static Account createEmpty(final User.Id userId, final Currency currency) {
        return new Account(Account.Id.generate(), userId, currency, BigDecimal.ZERO, empty());
    }

    public static class Id extends BaseId {
        private Id(final KSUID id) {
            super(id);
        }

        public static Id generate() {
            return with(KSUID.create());
        }

        public static Id with(final KSUID id) {
            return new Id(id);
        }
    }
}
