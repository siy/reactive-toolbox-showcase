package com.reactivetoolbox.domain;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;
import java.util.StringJoiner;

//TODO: in real application here should be much more information
public class UserDetails {
    private final String firstName;
    private final String lastName;

    private UserDetails(final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @JsonGetter("firstName")
    public String firstName() {
        return firstName;
    }

    @JsonGetter("lastName")
    public String lastName() {
        return lastName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDetails)) {
            return false;
        }
        final UserDetails that = (UserDetails) o;
        return firstName.equals(that.firstName) &&
               lastName.equals(that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "UserDetails(", ")")
                       .add("'" + firstName + "'")
                       .add("'" + lastName + "'")
                       .toString();
    }

    public static UserDetails details(final String firstName, final String lastName) {
        return new UserDetails(firstName, lastName);
    }
}
