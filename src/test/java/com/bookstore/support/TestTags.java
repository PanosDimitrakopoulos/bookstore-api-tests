package com.bookstore.support;


public final class TestTags {

    /** Fast, critical happy-path checks; a failure means the API is unusable. */
    public static final String SMOKE = "smoke";

    /** Invalid input and error handling. */
    public static final String NEGATIVE = "negative";

    /** Response structure validated against JSON schemas. */
    public static final String CONTRACT = "contract";

    /**
     * Expected REST behaviour the demo API does not implement. These tests fail by design
     * and are excluded from the gating run (see pom.xml).
     */
    public static final String KNOWN_DEFECT = "known-defect";

    private TestTags() {
    }
}
