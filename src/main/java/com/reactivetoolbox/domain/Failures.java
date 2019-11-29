package com.reactivetoolbox.domain;

import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.Result;
import org.reactivetoolbox.core.lang.support.WebFailureTypes;

import static org.reactivetoolbox.core.lang.Failure.failure;

public interface Failures {
    static <T> Result<T> insufficientFunds(final Account.Id accountId) {
        return failure(TransferFailureTypes.INSUFFICIENT_FUNDS, "Account {0} has insufficient funds", accountId)
                       .asResult();
    }

    static Failure accountNotFound(Account.Id accountId) {
        return failure(TransferFailureTypes.UNKNOWN_ACCOUNT, "Account {0} does not exist in the system", accountId);
    }

    static <T> Result<T> currencyDoesNotMatch(Currency opCurrency, Currency accountCurrency) {
        return failure(TransferFailureTypes.CURRENCY_MISMATCH, "Operation currency {0} does not match account currency {1}",
                       opCurrency.id(), accountCurrency.id()).asResult();
    }

    static Failure invalidParameter(String name) {
        return failure(WebFailureTypes.UNPROCESSABLE_ENTITY, "Input parameter {0} is missing or invalid", name);
    }
}
