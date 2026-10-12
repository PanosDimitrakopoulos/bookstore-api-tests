package com.bookstore.api.data;

import com.bookstore.api.model.Book;
import net.datafaker.Faker;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;


public final class BookTestData {

    private static final int MIN_NEW_ID = 10_000;
    private static final int MAX_NEW_ID = 1_000_000;

    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);

    private BookTestData() {
    }

    public static Book validBook() {
        return validBook(randomNewId());
    }

    public static Book validBook(int id) {
        Faker faker = FAKER.get();
        return new Book(
                id,
                faker.book().title(),
                faker.lorem().sentence(12),
                faker.number().numberBetween(1, 2_000),
                faker.lorem().paragraph(),
                pastDate(faker.number().numberBetween(1, 3_650)));
    }

    public static Map<String, Object> validBookAsMap() {
        Book book = validBook();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", book.id());
        payload.put("title", book.title());
        payload.put("description", book.description());
        payload.put("pageCount", book.pageCount());
        payload.put("excerpt", book.excerpt());
        payload.put("publishDate", book.publishDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        return payload;
    }

    public static int randomNewId() {
        return FAKER.get().number().numberBetween(MIN_NEW_ID, MAX_NEW_ID);
    }

    private static OffsetDateTime pastDate(int daysAgo) {
        return OffsetDateTime.now(ZoneOffset.UTC)
                .minusDays(daysAgo)
                .truncatedTo(ChronoUnit.SECONDS);
    }
}
