package com.bookstore.tests.books;

import com.bookstore.api.assertion.JsonSchemas;
import com.bookstore.api.client.BooksClient;
import com.bookstore.api.data.BookTestData;
import com.bookstore.api.model.Book;
import com.bookstore.support.ReportLabels;
import com.bookstore.support.TestTags;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

import static com.bookstore.api.assertion.ModelAssert.assertSameModel;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Create a book")
@DisplayName("POST /api/v1/Books")
class CreateBookTest {

    private final BooksClient booksClient = new BooksClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and echoes back the created book")
    void createsBook() {
        Book newBook = BookTestData.validBook();

        Book created = assertThatResponse(booksClient.create(newBook))
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAs(Book.class);

        assertSameModel(created, newBook);
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the book JSON schema")
    void responseMatchesBookSchema() {
        assertThatResponse(booksClient.create(BookTestData.validBook()))
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.BOOK);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("boundaryBooks")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("accepts boundary and special-character values")
    void acceptsBoundaryValues(Book boundaryBook) {
        Book created = assertThatResponse(booksClient.create(boundaryBook))
                .hasStatusCode(200)
                .extractingBodyAs(Book.class);

        assertSameModel(created, boundaryBook);
    }

    static Stream<Arguments> boundaryBooks() {
        Book base = BookTestData.validBook();
        return Stream.of(
                arguments(Named.of("page count of 0", base.withPageCount(0))),
                arguments(Named.of("page count at Int32 max", base.withPageCount(Integer.MAX_VALUE))),
                arguments(Named.of("id at Int32 max", base.withId(Integer.MAX_VALUE))),
                arguments(Named.of("single-character title", base.withTitle("A"))),
                arguments(Named.of("1,000-character title", base.withTitle("T".repeat(1_000)))),
                arguments(Named.of("Unicode title (Greek, CJK, emoji)", base.withTitle("Το Βιβλίο – 書籍 📚"))),
                arguments(Named.of("empty optional text fields", base.withDescription("").withExcerpt(""))),
                arguments(Named.of("publish date far in the past",
                        base.withPublishDate(OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)))),
                arguments(Named.of("publish date in the future",
                        base.withPublishDate(OffsetDateTime.of(2100, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)))));
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 400 for malformed JSON")
    void rejectsMalformedJson() {
        Response response = booksClient.createRaw("{\"id\": 1, \"title\": ", ContentType.JSON);

        assertThatResponse(response).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an empty request body")
    void rejectsEmptyBody() {
        Response response = booksClient.createRaw("", ContentType.JSON);

        assertThatResponse(response).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 when the body is a JSON array instead of an object")
    void rejectsArrayBody() {
        Response response = booksClient.createRaw("[]", ContentType.JSON);

        assertThatResponse(response).hasStatusCode(400);
    }

    @ParameterizedTest(name = "{0} = {1}")
    @MethodSource("invalidFieldTypes")
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 when a field has the wrong type or is out of range")
    void rejectsInvalidFieldTypes(String field, Object invalidValue) {
        Map<String, Object> payload = BookTestData.validBookAsMap();
        payload.put(field, invalidValue);

        assertThatResponse(booksClient.create(payload)).hasStatusCode(400);
    }

    static Stream<Arguments> invalidFieldTypes() {
        return Stream.of(
                arguments("id", "abc"),
                arguments("id", 1.5),
                arguments("pageCount", "many pages"),
                arguments("pageCount", 2_147_483_648L),
                arguments("publishDate", "not-a-date"));
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 415 for a non-JSON content type")
    void rejectsUnsupportedMediaType() {
        Response response = booksClient.createRaw("title=Some Book", ContentType.TEXT);

        assertThatResponse(response).hasStatusCode(415);
    }
}
