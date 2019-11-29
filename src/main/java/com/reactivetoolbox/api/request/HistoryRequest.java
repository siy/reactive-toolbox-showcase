package com.reactivetoolbox.api.request;

import com.reactivetoolbox.domain.Account;
import org.reactivetoolbox.core.lang.Range;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;

public class HistoryRequest {
    private final Account.Id accountId;
    private final Range<ZonedDateTime> dateRange;

    private HistoryRequest(final Account.Id accountId, final Range<ZonedDateTime> dateRange) {
        this.accountId = accountId;
        this.dateRange = dateRange;
    }

    public Account.Id accountId() {
        return accountId;
    }

    public Range<ZonedDateTime> range() {
        return dateRange;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistoryRequest)) {
            return false;
        }
        final HistoryRequest that = (HistoryRequest) o;
        return accountId.equals(that.accountId) &&
               dateRange.equals(that.dateRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, dateRange);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "HistoryRequest(", ")")
                       .add("account" + accountId)
                       .add(dateRange.toString())
                       .toString();
    }

    public static HistoryRequest request(final Account.Id accountId, final Range<ZonedDateTime> range) {
        return new HistoryRequest(accountId, range);
    }
}
