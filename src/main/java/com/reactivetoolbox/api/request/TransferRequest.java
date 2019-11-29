package com.reactivetoolbox.api.request;

import com.reactivetoolbox.domain.Account;
import com.reactivetoolbox.domain.Currency;
import org.reactivetoolbox.core.meta.AppMetaRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public class TransferRequest {
    private final Account.Id fromAccount;
    private final Account.Id toAccount;
    private final BigDecimal amount;
    private final Currency currency;
    private final LocalDateTime timestamp;

    private TransferRequest(final Account.Id fromAccount,
                            final Account.Id toAccount,
                            final BigDecimal amount,
                            final Currency currency,
                            final LocalDateTime timestamp) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public Account.Id fromAccount() {
        return fromAccount;
    }

    public Account.Id toAccount() {
        return toAccount;
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
        if (!(o instanceof TransferRequest)) {
            return false;
        }
        final TransferRequest that = (TransferRequest) o;
        return fromAccount.equals(that.fromAccount) &&
               toAccount.equals(that.toAccount) &&
               amount.equals(that.amount) &&
               currency.equals(that.currency) &&
               timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromAccount, toAccount, amount, currency, timestamp);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "TransferRequest(", ")")
                       .add("from" + fromAccount)
                       .add("to" + toAccount)
                       .add(amount.toString() + " " + currency.toString())
                       .add(timestamp.toString())
                       .toString();
    }

    public static TransferRequest transferRequest(final Account.Id fromAccount,
                                                  final Account.Id toAccount,
                                                  final BigDecimal amount,
                                                  final Currency currency) {
        final var clock = AppMetaRepository.instance().get(Clock.class);
        final LocalDateTime now = LocalDateTime.now(clock);
        return new TransferRequest(fromAccount, toAccount, amount, currency, now);
    }
}
