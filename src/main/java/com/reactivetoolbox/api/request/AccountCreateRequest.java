package com.reactivetoolbox.api.request;

import com.reactivetoolbox.domain.Currency;
import com.reactivetoolbox.domain.User;

import java.util.Objects;
import java.util.StringJoiner;

public class AccountCreateRequest {
    private final User.Id userId;
    private final Currency currency;

    public AccountCreateRequest(final User.Id userId, final Currency currency) {
        this.userId = userId;
        this.currency = currency;
    }

    public User.Id userId() {
        return userId;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountCreateRequest)) {
            return false;
        }
        final AccountCreateRequest that = (AccountCreateRequest) o;
        return userId.equals(that.userId) &&
               currency.equals(that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, currency);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "AccountCreateRequest(", ")")
                       .add("user" + userId)
                       .add(currency.toString())
                       .toString();
    }

    public static AccountCreateRequest request(final User.Id userId, final Currency currency) {
        return new AccountCreateRequest(userId, currency);
    }
}
