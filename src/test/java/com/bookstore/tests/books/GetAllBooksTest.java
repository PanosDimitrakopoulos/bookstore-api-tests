package com.bookstore.tests.books;

import com.bookstore.api.assertion.JsonSchemas;
import com.bookstore.api.client.BooksClient;
import com.bookstore.api.config.ApiConfig;
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

import java.util.List;

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Retrieve all books")
@DisplayName("GET /api/v1/Books")
class GetAllBooksTest {

    private final BooksClient booksClient = new BooksClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and a non-empty JSON list of books")
    void returnsNonEmptyListOfBooks() {
        Response response = booksClient.getAll();

        List<Book> books = assertThatResponse(response)
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAsListOf(Book[].class);

        assertThat(books).isNotEmpty();
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the book list JSON schema")
    void responseMatchesBookListSchema() {
        Response response = booksClient.getAll();

        assertThatResponse(response)
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.BOOK_LIST);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("every book has a unique positive id, a title and a non-negative page count")
    void everyBookHasValidCoreFields() {
        List<Book> books = assertThatResponse(booksClient.getAll())
                .hasStatusCode(200)
                .extractingBodyAsListOf(Book[].class);

        assertThat(books)
                .extracting(Book::id)
                .doesNotContainNull()
                .doesNotHaveDuplicates()
                .allSatisfy(id -> assertThat(id).isPositive());
        assertThat(books)
                .extracting(Book::title)
                .allSatisfy(title -> assertThat(title).isNotBlank());
        assertThat(books)
                .extracting(Book::pageCount)
                .allSatisfy(pageCount -> assertThat(pageCount).isNotNegative());
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("responds within the configured time limit")
    void respondsWithinTimeLimit() {
        Response response = booksClient.getAll();

        assertThatResponse(response)
                .hasStatusCode(200)
                .respondedWithin(ApiConfig.get().maxResponseTimeMs());
    }
}
