package com.bookstore.api.client;

import com.bookstore.api.http.RequestSpecFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public final class AuthorsClient extends ResourceClient {

    private static final String AUTHORS_PATH = "/Authors";
    private static final String BOOK_ID_PARAM = "idBook";
    private static final String BY_BOOK_SUFFIX = "/authors/books/{" + BOOK_ID_PARAM + "}";

    public AuthorsClient() {
        this(RequestSpecFactory.defaultSpec());
    }

    public AuthorsClient(RequestSpecification baseSpec) {
        super(baseSpec, AUTHORS_PATH);
    }

    public Response getByBookId(Object bookId) {
        return request()
                .pathParam(BOOK_ID_PARAM, bookId)
                .get(resourcePath() + BY_BOOK_SUFFIX);
    }
}
