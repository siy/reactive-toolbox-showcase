package com.reactivetoolbox.domain;

import org.reactivetoolbox.core.lang.support.KSUID;

public class BaseId {
    private final KSUID id;

    protected BaseId(final KSUID id) {
        this.id = id;
    }

    public KSUID id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof BaseId) && id.equals(((BaseId) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "id(" + id.encoded() + ")";
    }
}
