package com.bookstore.api.data;

import com.bookstore.api.model.Author;
import net.datafaker.Faker;

import java.util.LinkedHashMap;
import java.util.Map;


public final class AuthorTestData {

    private static final int MIN_NEW_ID = 10_000;
    private static final int MAX_NEW_ID = 1_000_000;

    private static final int MIN_EXISTING_BOOK_ID = 1;
    private static final int MAX_EXISTING_BOOK_ID = 200;

    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);

    private AuthorTestData() {
    }

    public static Author validAuthor() {
        return validAuthor(randomNewId());
    }

    public static Author validAuthor(int id) {
        Faker faker = FAKER.get();
        return new Author(
                id,
                faker.number().numberBetween(MIN_EXISTING_BOOK_ID, MAX_EXISTING_BOOK_ID + 1),
                faker.name().firstName(),
                faker.name().lastName());
    }

    public static Map<String, Object> validAuthorAsMap() {
        Author author = validAuthor();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", author.id());
        payload.put("idBook", author.idBook());
        payload.put("firstName", author.firstName());
        payload.put("lastName", author.lastName());
        return payload;
    }

    public static int randomNewId() {
        return FAKER.get().number().numberBetween(MIN_NEW_ID, MAX_NEW_ID);
    }
}
