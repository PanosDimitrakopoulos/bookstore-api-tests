package com.bookstore.tests.books;

import com.bookstore.api.assertion.JsonSchemas;
import com.bookstore.api.client.BooksClient;
import com.bookstore.api.model.Book;
import com.bookstore.support.ReportLabels;
import com.bookstore.support.TestTags;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.bookstore.api.assertion.ModelAssert.assertSameModelIgnoringFields;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Retrieve a book by id")
@DisplayName("GET /api/v1/Books/{id}")
class GetBookByIdTest {

    private final BooksClient booksClient = new BooksClient();

    @ParameterizedTest(name = "returns book {0}")
    @ValueSource(ints = {1, 50, 100})
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and the requested book")
    void returnsRequestedBook(int bookId) {
        Book book = assertThatResponse(booksClient.getById(bookId))
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAs(Book.class);

        assertThat(book.id()).isEqualTo(bookId);
        assertThat(book.title()).isNotBlank();
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the book JSON schema")
    void responseMatchesBookSchema() {
        assertThatResponse(booksClient.getById(1))
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.BOOK);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns the same book as the list endpoint (last book in the list)")
    void returnsSameDataAsListEndpoint() {
        List<Book> books = assertThatResponse(booksClient.getAll())
                .hasStatusCode(200)
                .extractingBodyAsListOf(Book[].class);
        assertThat(books).isNotEmpty();
        Book expected = books.get(books.size() - 1);

        Book actual = assertThatResponse(booksClient.getById(expected.id()))
                .hasStatusCode(200)
                .extractingBodyAs(Book.class);

        // The demo API generates publishDate relative to "now" on every request, so it can't be compared.
        assertSameModelIgnoringFields(actual, expected, "publishDate");
    }

    @ParameterizedTest(name = "id {0} returns 404")
    @ValueSource(ints = {0, -1, 999_999, Integer.MAX_VALUE})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 404 for an id that does not exist")
    void returnsNotFoundForUnknownId(int unknownId) {
        Response response = booksClient.getById(unknownId);

        assertThatResponse(response).hasStatusCode(404);
    }

    @ParameterizedTest(name = "id \"{0}\" returns 400")
    @ValueSource(strings = {"abc", "1.5", "1e3", "2147483648"})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not a valid 32-bit integer")
    void returnsBadRequestForMalformedId(String malformedId) {
        Response response = booksClient.getById(malformedId);

        assertThatResponse(response).hasStatusCode(400);
    }
}
