package com.reactivetoolbox.domain;

import java.util.Objects;

public interface Currency {
    Currency.Id id();

    class Id {
        private final String id;

        private Id(final String id) {
            this.id = id;
        }

        static Id with(final String id) {
            return new Id(id);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            return o instanceof Id && id.equals(((Id) o).id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
