package com.bookstore.tests.authors;

import com.bookstore.api.assertion.JsonSchemas;
import com.bookstore.api.client.AuthorsClient;
import com.bookstore.api.data.AuthorTestData;
import com.bookstore.api.model.Author;
import com.bookstore.support.ReportLabels;
import com.bookstore.support.TestTags;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.bookstore.api.assertion.ModelAssert.assertSameModel;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Update an author")
@DisplayName("PUT /api/v1/Authors/{id}")
class UpdateAuthorTest {

    private static final int EXISTING_AUTHOR_ID = 10;

    private final AuthorsClient authorsClient = new AuthorsClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and echoes back the updated author")
    void updatesExistingAuthor() {
        Author changes = AuthorTestData.validAuthor(EXISTING_AUTHOR_ID);

        Author updated = assertThatResponse(authorsClient.update(EXISTING_AUTHOR_ID, changes))
                .hasStatusCode(200)
                .hasJsonContentType()
                .matchesJsonSchema(JsonSchemas.AUTHOR)
                .extractingBodyAs(Author.class);

        assertSameModel(updated, changes);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not an integer")
    void rejectsMalformedId() {
        assertThatResponse(authorsClient.update("abc", AuthorTestData.validAuthor(EXISTING_AUTHOR_ID)))
                .hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 400 for malformed JSON")
    void rejectsMalformedJson() {
        assertThatResponse(authorsClient.updateRaw(EXISTING_AUTHOR_ID, "{\"lastName\": \"unterminated", ContentType.JSON))
                .hasStatusCode(400);
    }
}
