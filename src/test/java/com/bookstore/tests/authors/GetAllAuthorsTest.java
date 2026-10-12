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

import java.util.List;

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Retrieve all authors")
@DisplayName("GET /api/v1/Authors")
class GetAllAuthorsTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and a non-empty JSON list of authors")
    void returnsNonEmptyListOfAuthors() {
        List<Author> authors = assertThatResponse(authorsClient.getAll())
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAsListOf(Author[].class);

        assertThat(authors).isNotEmpty();
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the author list JSON schema")
    void responseMatchesAuthorListSchema() {
        assertThatResponse(authorsClient.getAll())
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.AUTHOR_LIST);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("every author has a unique positive id and references a book")
    void everyAuthorHasValidCoreFields() {
        List<Author> authors = assertThatResponse(authorsClient.getAll())
                .hasStatusCode(200)
                .extractingBodyAsListOf(Author[].class);

        assertThat(authors)
                .extracting(Author::id)
                .doesNotContainNull()
                .doesNotHaveDuplicates()
                .allSatisfy(id -> assertThat(id).isPositive());
        assertThat(authors)
                .extracting(Author::idBook)
                .allSatisfy(idBook -> assertThat(idBook).isPositive());
    }
}
