package com.reactivetoolbox.api.response;

import org.reactivetoolbox.core.lang.Failure;

import java.util.Objects;
import java.util.StringJoiner;

public class Status {
    private final int code;
    private final String message;

    private Status(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Status)) {
            return false;
        }
        final Status status = (Status) o;
        return code == status.code &&
               message.equals(status.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Status(", ")")
                       .add(Integer.toString(code))
                       .add("'" + message + "'")
                       .toString();
    }

    public static Status success() {
        return new Status(0, "Operation successful");
    }

    public static Status failure(final Failure failure) {
        return new Status(failure.type().code(), failure.message());
    }
}
