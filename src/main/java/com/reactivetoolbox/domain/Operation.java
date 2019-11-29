package com.reactivetoolbox.domain;

import org.reactivetoolbox.core.lang.Option;
import org.reactivetoolbox.core.lang.Tuple;
import org.reactivetoolbox.core.lang.Tuple.Tuple2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import static org.reactivetoolbox.core.lang.Option.empty;
import static org.reactivetoolbox.core.lang.Option.option;

public class Operation {
    private final Transfer.Id transferId;
    private final Option<Transfer.Id> previousId;

    private final Account.Id local;
    private final Account.Id remote;

    private final BigDecimal amount;
    private final Option<BigDecimal> balance;

    private final Currency currency;
    private final LocalDateTime timestamp;

    private Operation(final Transfer.Id transferId,
                      final Option<Transfer.Id> previousTransferId,
                      final Account.Id local,
                      final Account.Id remote,
                      final BigDecimal amount,
                      final Option<BigDecimal> balance,
                      final Currency currency,
                      final LocalDateTime timestamp) {
        this.transferId = transferId;
        this.previousId = previousTransferId;
        this.local = local;
        this.remote = remote;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.balance = balance;
    }

    public BigDecimal amount() {
        return amount;
    }

    public Transfer.Id transferId() {
        return transferId;
    }

    public Account.Id local() {
        return local;
    }

    public Account.Id remote() {
        return remote;
    }

    public LocalDateTime timestamp() {
        return timestamp;
    }

    public Option<Transfer.Id> previousTransferId() {
        return previousId;
    }

    public Currency currency() {
        return currency;
    }

    public Option<BigDecimal> balance() {
        return balance;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Operation)) {
            return false;
        }
        final Operation operation = (Operation) o;
        return transferId.equals(operation.transferId) &&
               local.equals(operation.local) &&
               remote.equals(operation.remote) &&
               amount.equals(operation.amount) &&
               currency.equals(operation.currency) &&
               timestamp.equals(operation.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, local, remote, amount, currency, timestamp);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Operation(", ")")
                       .add("transfer" + transferId)
                       .add(previousId.toString())
                       .add("local" + local)
                       .add("remote" + remote)
                       .add(amount.toString() + " " + currency().id())
                       .add(balance.toString())
                       .add(timestamp.toString())
                       .toString();
    }

    public Operation chain(final Option<Transfer.Id> previousTransferId, final BigDecimal balance) {
        return new Operation(transferId,
                             previousTransferId,
                             local,
                             remote,
                             amount,
                             option(balance), currency,
                             timestamp
        );
    }

    public static Tuple2<Operation, Operation> split(final Transfer transfer) {
        return Tuple.tuple(new Operation(transfer.id(),
                                         empty(),
                                         transfer.fromAccount(),
                                         transfer.toAccount(),
                                         transfer.amount().negate(),
                                         empty(), transfer.currency(),
                                         transfer.timestamp()),
                           new Operation(transfer.id(),
                                         empty(),
                                         transfer.toAccount(),
                                         transfer.fromAccount(),
                                         transfer.amount(),
                                         empty(), transfer.currency(),
                                         transfer.timestamp()));
    }
}
