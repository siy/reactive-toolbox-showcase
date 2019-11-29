package com.reactivetoolbox.domain;

import com.reactivetoolbox.api.request.TransferRequest;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;
import org.reactivetoolbox.core.lang.support.KSUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import static org.reactivetoolbox.core.lang.Tuple.tuple;

public class Transfer {
    private final Transfer.Id id;
    private final Account.Id fromAccount;
    private final Account.Id toAccount;
    private final BigDecimal amount;
    private final Currency currency;
    private final LocalDateTime timestamp;

    private Transfer(final Transfer.Id id,
                     final Account.Id fromAccount,
                     final Account.Id toAccount,
                     final BigDecimal amount,
                     final Currency currency,
                     final LocalDateTime timestamp) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public Transfer.Id id() {
        return id;
    }

    public Account.Id fromAccount() {
        return fromAccount;
    }

    public Account.Id toAccount() {
        return toAccount;
    }

    public Tuple2<Account.Id, Account.Id> accounts() {
        return tuple(fromAccount, toAccount);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public LocalDateTime timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transfer)) {
            return false;
        }
        final Transfer transfer = (Transfer) o;
        return id.equals(transfer.id) &&
               fromAccount.equals(transfer.fromAccount) &&
               toAccount.equals(transfer.toAccount) &&
               amount.equals(transfer.amount) &&
               currency.equals(transfer.currency) &&
               timestamp.equals(transfer.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromAccount, toAccount, amount, currency, timestamp);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Transfer(", ")")
                       .add(id.toString())
                       .add(fromAccount.toString())
                       .add(toAccount.toString())
                       .add(amount.toString())
                       .add(currency.toString())
                       .add(timestamp.toString())
                       .toString();
    }

    public static Transfer from(final TransferRequest request) {
        return new Transfer(Id.generate(), request.fromAccount(), request.toAccount(), request.amount(), request.currency(), request.timestamp());
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
