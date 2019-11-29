package com.reactivetoolbox.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.reactivetoolbox.core.lang.support.KSUID;

import java.util.Objects;
import java.util.StringJoiner;

public class User {
    private final Id id;
    private final UserDetails details;

    private User(final Id id, final UserDetails details) {
        this.id = id;
        this.details = details;
    }

    @JsonGetter("id")
    public User.Id id() {
        return id;
    }

    @JsonGetter("details")
    public UserDetails details() {
        return details;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        final User user = (User) o;
        return id.equals(user.id) &&
               details.equals(user.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, details);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "User(", ")")
                       .add(id.toString())
                       .add(details.toString())
                       .toString();
    }

    public static User user(final Id userId, final UserDetails details) {
        return new User(userId, details);
    }

    public static User create(final String firstName, final String lastName) {
        return user(Id.generate(), UserDetails.details(firstName, lastName));
    }

    public static class Id extends BaseId {
        private Id(final KSUID id) {
            super(id);
        }

        public static Id generate() {
            return with(KSUID.create());
        }

        public static Id with(final KSUID id) {
            return new Id(id);
        }
    }
}
