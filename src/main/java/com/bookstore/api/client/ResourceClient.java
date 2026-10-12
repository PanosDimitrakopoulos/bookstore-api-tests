package com.bookstore.api.client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

public abstract class ResourceClient {

    private static final String ID_PARAM = "id";
    private static final String BY_ID_SUFFIX = "/{" + ID_PARAM + "}";

    private final RequestSpecification baseSpec;
    private final String resourcePath;

    protected ResourceClient(RequestSpecification baseSpec, String resourcePath) {
        this.baseSpec = Objects.requireNonNull(baseSpec, "baseSpec must not be null");
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath must not be null");
    }

    public Response getAll() {
        return request().get(resourcePath);
    }

    public Response getById(Object id) {
        return request()
                .pathParam(ID_PARAM, id)
                .get(resourcePath + BY_ID_SUFFIX);
    }

    public Response create(Object payload) {
        return request()
                .body(payload)
                .post(resourcePath);
    }

    public Response createRaw(String rawBody, ContentType contentType) {
        return request()
                .contentType(contentType)
                .body(rawBody)
                .post(resourcePath);
    }

    public Response update(Object id, Object payload) {
        return request()
                .pathParam(ID_PARAM, id)
                .body(payload)
                .put(resourcePath + BY_ID_SUFFIX);
    }

    public Response updateRaw(Object id, String rawBody, ContentType contentType) {
        return request()
                .pathParam(ID_PARAM, id)
                .contentType(contentType)
                .body(rawBody)
                .put(resourcePath + BY_ID_SUFFIX);
    }

    public Response delete(Object id) {
        return request()
                .pathParam(ID_PARAM, id)
                .delete(resourcePath + BY_ID_SUFFIX);
    }

    protected RequestSpecification request() {
        return RestAssured.given().spec(baseSpec);
    }

    protected String resourcePath() {
        return resourcePath;
    }
}
