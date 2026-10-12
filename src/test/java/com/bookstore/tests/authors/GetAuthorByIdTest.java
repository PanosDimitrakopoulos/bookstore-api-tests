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

import static com.bookstore.api.assertion.ModelAssert.assertSameModel;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Retrieve an author by id")
@DisplayName("GET /api/v1/Authors/{id}")
class GetAuthorByIdTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @ParameterizedTest(name = "returns author {0}")
    @ValueSource(ints = {1, 10})
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and the requested author")
    void returnsRequestedAuthor(int authorId) {
        Author author = assertThatResponse(authorsClient.getById(authorId))
                .hasStatusCode(200)
                .hasJsonContentType()
                .extractingBodyAs(Author.class);

        assertThat(author.id()).isEqualTo(authorId);
    }

    @Test
    @Tag(TestTags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("response matches the author JSON schema")
    void responseMatchesAuthorSchema() {
        assertThatResponse(authorsClient.getById(1))
                .hasStatusCode(200)
                .matchesJsonSchema(JsonSchemas.AUTHOR);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns the same author as the list endpoint (first author in the list)")
    void returnsSameDataAsListEndpoint() {
        List<Author> authors = assertThatResponse(authorsClient.getAll())
                .hasStatusCode(200)
                .extractingBodyAsListOf(Author[].class);
        assertThat(authors).isNotEmpty();
        Author expected = authors.get(0);

        Author actual = assertThatResponse(authorsClient.getById(expected.id()))
                .hasStatusCode(200)
                .extractingBodyAs(Author.class);

        assertSameModel(actual, expected);
    }

    @ParameterizedTest(name = "id {0} returns 404")
    @ValueSource(ints = {0, -1, 999_999})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 404 for an id that does not exist")
    void returnsNotFoundForUnknownId(int unknownId) {
        assertThatResponse(authorsClient.getById(unknownId)).hasStatusCode(404);
    }

    @ParameterizedTest(name = "id \"{0}\" returns 400")
    @ValueSource(strings = {"abc", "1.5", "2147483648"})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not a valid 32-bit integer")
    void returnsBadRequestForMalformedId(String malformedId) {
        assertThatResponse(authorsClient.getById(malformedId)).hasStatusCode(400);
    }
}
