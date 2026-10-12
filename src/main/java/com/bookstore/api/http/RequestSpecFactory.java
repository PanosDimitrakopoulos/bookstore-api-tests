package com.bookstore.api.http;

import com.bookstore.api.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;


public final class RequestSpecFactory {

    private RequestSpecFactory() {
    }

    public static RequestSpecification defaultSpec() {
        return defaultSpec(ApiConfig.get());
    }

    public static RequestSpecification defaultSpec(ApiConfig config) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.baseUrl())
                .setBasePath(config.basePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(restAssuredConfig(config))
                // Attaches every request and response to the Allure report
                .addFilter(new AllureRestAssured());

        if (config.isHttpLoggingEnabled()) {
            builder.addFilter(new RequestLoggingFilter())
                    .addFilter(new ResponseLoggingFilter());
        }
        return builder.build();
    }

    private static RestAssuredConfig restAssuredConfig(ApiConfig config) {
        return RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> JsonMapperFactory.create()))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", config.connectTimeoutMs())
                        .setParam("http.socket.timeout", config.readTimeoutMs()));
    }
}
