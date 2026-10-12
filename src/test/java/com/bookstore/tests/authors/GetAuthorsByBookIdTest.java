package com.bookstore.tests.authors;

import com.bookstore.api.assertion.JsonSchemas;
import com.bookstore.api.client.AuthorsClient;
import com.bookstore.api.model.Author;
import com.bookstore.support.ReportLabels;
import com.bookstore.support.TestTags;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Retrieve the authors of a book")
@DisplayName("GET /api/v1/Authors/authors/books/{idBook}")
class GetAuthorsByBookIdTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @ParameterizedTest(name = "authors of book {0}")
    @ValueSource(ints = {1, 25})
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns only authors linked to the requested book")
    void returnsOnlyAuthorsOfRequestedBook(int bookId) {
        List<Author> authors = assertThatResponse(authorsClient.getByBookId(bookId))
                .hasStatusCode(200)
                .hasJsonContentType()
                .matchesJsonSchema(JsonSchemas.AUTHOR_LIST)
                .extractingBodyAsListOf(Author[].class);

        assertThat(authors)
                .isNotEmpty()
                .allSatisfy(author -> assertThat(author.idBook()).isEqualTo(bookId));
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns an empty list for a book without authors")
    void returnsEmptyListForUnknownBook() {
        List<Author> authors = assertThatResponse(authorsClient.getByBookId(999_999))
                .hasStatusCode(200)
                .extractingBodyAsListOf(Author[].class);

        assertThat(authors).isEmpty();
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for a book id that is not an integer")
    void rejectsMalformedBookId() {
        assertThatResponse(authorsClient.getByBookId("abc")).hasStatusCode(400);
    }
}
