package com.reactivetoolbox.api.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.reactivetoolbox.domain.Operation;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.List;

import java.util.Objects;
import java.util.StringJoiner;

public class HistoryResponse {
    private final List<Operation> operations;
    private final Status status;

    private HistoryResponse(final List<Operation> operations, final Status status) {
        this.operations = operations;
        this.status = status;
    }

    @JsonGetter("operations")
    public List<Operation> operations() {
        return operations;
    }

    @JsonGetter("status")
    public Status status() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistoryResponse)) {
            return false;
        }
        final HistoryResponse that = (HistoryResponse) o;
        return operations.equals(that.operations) &&
               status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operations, status);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "HistoryResponse(", ")")
                       .add(status.toString())
                       .add(operations.toString())
                       .toString();
    }

    public static HistoryResponse success(final List<Operation> operations) {
        return new HistoryResponse(operations, Status.success());
    }

    public static HistoryResponse failure(final Failure failure) {
        return new HistoryResponse(List.list(), Status.failure(failure));
    }
}
