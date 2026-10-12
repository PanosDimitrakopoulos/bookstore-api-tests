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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static com.bookstore.api.assertion.ModelAssert.assertSameModel;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Update a book")
@DisplayName("PUT /api/v1/Books/{id}")
class UpdateBookTest {

    private static final int EXISTING_BOOK_ID = 10;

    private final BooksClient booksClient = new BooksClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and echoes back the updated book")
    void updatesExistingBook() {
        Book changes = BookTestData.validBook(EXISTING_BOOK_ID);

        Book updated = assertThatResponse(booksClient.update(EXISTING_BOOK_ID, changes))
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAs(Book.class);

        assertSameModel(updated, changes);
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the book JSON schema")
    void responseMatchesBookSchema() {
        assertThatResponse(booksClient.update(EXISTING_BOOK_ID, BookTestData.validBook(EXISTING_BOOK_ID)))
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.BOOK);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("applies a change to a single field and keeps the others")
    void updatesSingleField() {
        Book original = BookTestData.validBook(EXISTING_BOOK_ID);
        Book renamed = original.withTitle("Renamed: " + original.title());

        Book updated = assertThatResponse(booksClient.update(EXISTING_BOOK_ID, renamed))
                .hasStatusCode(200)
                .extractingBodyAs(Book.class);

        assertSameModel(updated, renamed);
    }

    @ParameterizedTest(name = "id \"{0}\" returns 400")
    @ValueSource(strings = {"abc", "1.5"})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not a valid integer")
    void rejectsMalformedId(String malformedId) {
        Response response = booksClient.update(malformedId, BookTestData.validBook(EXISTING_BOOK_ID));

        assertThatResponse(response).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 400 for malformed JSON")
    void rejectsMalformedJson() {
        Response response = booksClient.updateRaw(EXISTING_BOOK_ID, "{\"title\": \"unterminated", ContentType.JSON);

        assertThatResponse(response).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an empty request body")
    void rejectsEmptyBody() {
        Response response = booksClient.updateRaw(EXISTING_BOOK_ID, "", ContentType.JSON);

        assertThatResponse(response).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 when pageCount is not a number")
    void rejectsInvalidFieldType() {
        Map<String, Object> payload = BookTestData.validBookAsMap();
        payload.put("id", EXISTING_BOOK_ID);
        payload.put("pageCount", "a lot");

        assertThatResponse(booksClient.update(EXISTING_BOOK_ID, payload)).hasStatusCode(400);
    }
}
