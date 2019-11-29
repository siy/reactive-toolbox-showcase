package com.reactivetoolbox.domain;

import org.reactivetoolbox.core.lang.Result;

import java.util.Locale;

import static com.reactivetoolbox.domain.Currency.Id.with;
import static org.reactivetoolbox.core.lang.ThrowingFunctions.wrap;

public enum SupportedCurrencies implements Currency {
    GBP, EUR, USD;

    @Override
    public Id id() {
        return with(name());
    }

    public static Result<Currency> fromString(final String value) {
        return wrap(() -> valueOf(value.toUpperCase(Locale.UK)));
    }
}
