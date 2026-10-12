package com.bookstore.api.assertion;

import java.time.OffsetDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;


public final class ModelAssert {

    @SuppressWarnings("null")
    private static final Comparator<OffsetDateTime> SAME_INSTANT = Comparator.comparing(OffsetDateTime::toInstant);

    private ModelAssert() {
    }

    public static <T> void assertSameModel(T actual, T expected) {
        assertThat(actual)
                .usingRecursiveComparison()
                .withComparatorForType(SAME_INSTANT, OffsetDateTime.class)
                .isEqualTo(expected);
    }

    public static <T> void assertSameModelIgnoringFields(T actual, T expected, String... ignoredFields) {
        assertThat(actual)
                .usingRecursiveComparison()
                .withComparatorForType(SAME_INSTANT, OffsetDateTime.class)
                .ignoringFields(ignoredFields)
                .isEqualTo(expected);
    }
}
