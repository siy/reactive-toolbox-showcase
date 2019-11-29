package com.reactivetoolbox.api.response;

import com.reactivetoolbox.domain.Transfer;
import org.reactivetoolbox.core.lang.Failure;
import org.reactivetoolbox.core.lang.Option;

import java.util.Objects;
import java.util.StringJoiner;

public class TransferResponse {
    private final Option<Transfer.Id> transferId;
    private final Status status;

    private TransferResponse(final Option<Transfer.Id> transferId, final Status status) {
        this.transferId = transferId;
        this.status = status;
    }

    public Option<Transfer.Id> transferId() {
        return transferId;
    }

    public Status status() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransferResponse)) {
            return false;
        }
        final TransferResponse that = (TransferResponse) o;
        return transferId.equals(that.transferId) &&
               status.equals(that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, status);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "TransferResponse(", ")")
                       .add(transferId.map(id -> "transfer" + id).otherwise("''"))
                       .add(status.toString())
                       .toString();
    }

    public static TransferResponse success(final Transfer.Id transferId) {
        return new TransferResponse(Option.option(transferId), Status.success());
    }

    public static TransferResponse failure(final Failure failure) {
        return new TransferResponse(Option.empty(), Status.failure(failure));
    }
}
