package com.reactivetoolbox.domain;

import org.reactivetoolbox.core.lang.FailureType;

public enum TransferFailureTypes implements FailureType {
    UNKNOWN_ACCOUNT(1, "Account is not present in the system"),
    INSUFFICIENT_FUNDS(2, "Account has insufficient funds to withdraw"),
    CURRENCY_MISMATCH(3, "Account and operation currency mismatch");
    ;

    private final int code;
    private final String description;

    TransferFailureTypes(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String description() {
        return description;
    }
}
