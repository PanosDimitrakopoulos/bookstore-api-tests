package com.bookstore.api.assertion;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


public final class ResponseAssert {

    private static final int MAX_BODY_IN_MESSAGE = 500;

    private final Response response;

    private ResponseAssert(Response response) {
        this.response = response;
    }

    public static ResponseAssert assertThatResponse(Response response) {
        return new ResponseAssert(Objects.requireNonNull(response, "response must not be null"));
    }

    public ResponseAssert hasStatusCode(int expectedStatusCode) {
        assertThat(response.statusCode())
                .as("HTTP status code. Response body: %s", abbreviatedBody())
                .isEqualTo(expectedStatusCode);
        return this;
    }

    public ResponseAssert hasJsonContentType() {
        assertThat(response.contentType())
                .as("Content-Type header")
                .containsIgnoringCase("application/json");
        return this;
    }

    public ResponseAssert hasEmptyBody() {
        assertThat(response.asString())
                .as("Response body")
                .isEmpty();
        return this;
    }

    /** Validates the body against a JSON schema on the classpath (see {@link JsonSchemas}). */
    public ResponseAssert matchesJsonSchema(String schemaClasspathLocation) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaClasspathLocation));
        return this;
    }

    public ResponseAssert respondedWithin(long maxMillis) {
        assertThat(response.getTime())
                .as("Response time in ms")
                .isLessThanOrEqualTo(maxMillis);
        return this;
    }

    public <T> T extractingBodyAs(Class<T> type) {
        return response.as(type);
    }

    public <T> List<T> extractingBodyAsListOf(Class<T[]> arrayType) {
        return Arrays.asList(response.as(arrayType));
    }

    private String abbreviatedBody() {
        String body = response.asString();
        if (body == null || body.isEmpty()) {
            return "<empty>";
        }
        return body.length() <= MAX_BODY_IN_MESSAGE ? body : body.substring(0, MAX_BODY_IN_MESSAGE) + "...";
    }
}
