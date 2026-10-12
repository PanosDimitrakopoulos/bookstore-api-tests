package com.bookstore.api.client;

import com.bookstore.api.http.RequestSpecFactory;
import io.restassured.specification.RequestSpecification;


public final class BooksClient extends ResourceClient {

    private static final String BOOKS_PATH = "/Books";

    public BooksClient() {
        this(RequestSpecFactory.defaultSpec());
    }

    public BooksClient(RequestSpecification baseSpec) {
        super(baseSpec, BOOKS_PATH);
    }
}
