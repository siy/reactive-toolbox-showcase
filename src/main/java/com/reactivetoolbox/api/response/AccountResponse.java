package com.reactivetoolbox.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reactivetoolbox.domain.Account;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.Option;

import java.util.Objects;
import java.util.StringJoiner;

public class AccountResponse {
    private final Option<Account> account;
    private final Status status;

    private AccountResponse(final Option<Account> account, final Status status) {
        this.account = account;
        this.status = status;
    }

    @JsonProperty("account")
    public Option<Account> account() {
        return account;
    }

    @JsonProperty("status")
    public Status status() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountResponse)) {
            return false;
        }
        final AccountResponse that = (AccountResponse) o;
        return account.equals(that.account) &&
               status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, status);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "AccountResponse(", ")")
                       .add(account.map(Objects::toString).otherwise("<none>"))
                       .add(status.toString())
                       .toString();
    }

    public static AccountResponse success(final Account account) {
        return new AccountResponse(Option.option(account), Status.success());
    }

    public static AccountResponse failure(final Failure failure) {
        return new AccountResponse(Option.empty(), Status.failure(failure));
    }
}
