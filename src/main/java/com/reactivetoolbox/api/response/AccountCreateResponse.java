package com.reactivetoolbox.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reactivetoolbox.domain.Account;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.Option;

import java.util.Objects;
import java.util.StringJoiner;

public class AccountCreateResponse {
    private final Option<Account.Id> accountId;
    private final Status status;

    private AccountCreateResponse(final Option<Account.Id> accountId, final Status status) {
        this.accountId = accountId;
        this.status = status;
    }

    @JsonProperty("accountId")
    public Option<Account.Id> accountId() {
        return accountId;
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
        if (!(o instanceof AccountCreateResponse)) {
            return false;
        }
        final AccountCreateResponse that = (AccountCreateResponse) o;
        return accountId.equals(that.accountId) &&
               status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, status);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "AccountCreateResponse(", ")")
                       .add(accountId.map(id -> "account" + id).otherwise("<none>"))
                       .add(status.toString())
                       .toString();
    }

    public static AccountCreateResponse success(final Account.Id accountId) {
        return new AccountCreateResponse(Option.option(accountId), Status.success());
    }

    public static AccountCreateResponse failure(final Failure failure) {
        return new AccountCreateResponse(Option.empty(), Status.failure(failure));
    }
}
