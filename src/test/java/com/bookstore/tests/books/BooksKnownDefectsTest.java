package com.bookstore.tests.books;

import com.bookstore.api.client.BooksClient;
import com.bookstore.api.data.BookTestData;
import com.bookstore.api.model.Book;
import com.bookstore.support.ReportLabels;
import com.bookstore.support.TestTags;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static com.bookstore.support.ReportLabels.KNOWN_DEFECT_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@Tag(TestTags.KNOWN_DEFECT)
@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.BOOKS)
@Story("Known API defects")
@DisplayName("Books API: known defects")
class BooksKnownDefectsTest {

    private final BooksClient booksClient = new BooksClient();

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("POST should return 201 Created")
    @Description("RFC 9110: a request that creates a resource should answer 201 Created. The API answers 200 OK.")
    void createShouldReturnCreated() {
        Response response = booksClient.create(BookTestData.validBook());

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "POST /Books should return 201 Created")
                .isEqualTo(201);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("a created book should be retrievable by its id")
    @Description("The API echoes the new book but does not store it, so a follow-up GET returns 404.")
    void createdBookShouldBePersisted() {
        Book newBook = BookTestData.validBook();
        assertThatResponse(booksClient.create(newBook)).hasStatusCode(200);

        Response lookup = booksClient.getById(newBook.id());

        assertThat(lookup.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "GET /Books/%d right after creating it should return 200", newBook.id())
                .isEqualTo(200);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("a deleted book should no longer be retrievable")
    @Description("DELETE answers 200 but the book is still returned by GET afterwards.")
    void deletedBookShouldBeGone() {
        int bookId = 5;
        assertThatResponse(booksClient.delete(bookId)).hasStatusCode(200);

        Response lookup = booksClient.getById(bookId);

        assertThat(lookup.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "GET /Books/%d after deleting it should return 404", bookId)
                .isEqualTo(404);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE of a non-existent book should return 404")
    @Description("The API answers 200 for any integer id, so clients can't tell a real delete from a no-op.")
    void deleteUnknownBookShouldReturnNotFound() {
        Response response = booksClient.delete(999_999);

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "DELETE /Books/999999 should return 404")
                .isEqualTo(404);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT of a non-existent book should return 404")
    @Description("The API answers 200 and echoes the body even though there is nothing to update.")
    void updateUnknownBookShouldReturnNotFound() {
        Response response = booksClient.update(999_999, BookTestData.validBook(999_999));

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "PUT /Books/999999 should return 404")
                .isEqualTo(404);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PUT should reject a body whose id differs from the path id")
    @Description("Path id 10 with body id 20 is accepted, leaving it ambiguous which book was updated.")
    void updateWithMismatchedIdsShouldBeRejected() {
        Response response = booksClient.update(10, BookTestData.validBook(20));

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "PUT /Books/10 with body id 20 should return 400")
                .isEqualTo(400);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST should reject a book with a blank title")
    @Description("There is no input validation: a book with an empty title is accepted.")
    void createWithBlankTitleShouldBeRejected() {
        Response response = booksClient.create(BookTestData.validBook().withTitle(""));

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "POST /Books with an empty title should return 400")
                .isEqualTo(400);
    }
}
