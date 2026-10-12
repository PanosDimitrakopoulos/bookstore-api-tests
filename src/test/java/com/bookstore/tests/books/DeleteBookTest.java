package com.bookstore.tests.books;

import com.bookstore.api.client.BooksClient;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Delete a book")
@DisplayName("DELETE /api/v1/Books/{id}")
class DeleteBookTest {

    private final BooksClient booksClient = new BooksClient();

    @ParameterizedTest(name = "deletes book {0}")
    @ValueSource(ints = {1, 100})
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 with an empty body for an existing book")
    void deletesExistingBook(int bookId) {
        Response response = booksClient.delete(bookId);

        assertThatResponse(response)
                .hasStatusCode(200)
                .hasEmptyBody();
    }

    @ParameterizedTest(name = "id \"{0}\" returns 400")
    @ValueSource(strings = {"abc", "1.5", "2147483648"})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not a valid 32-bit integer")
    void rejectsMalformedId(String malformedId) {
        Response response = booksClient.delete(malformedId);

        assertThatResponse(response).hasStatusCode(400);
    }
}
