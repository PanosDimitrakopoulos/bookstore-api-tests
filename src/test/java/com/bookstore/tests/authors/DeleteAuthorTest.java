package com.bookstore.tests.authors;

import com.bookstore.api.client.AuthorsClient;
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

import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Delete an author")
@DisplayName("DELETE /api/v1/Authors/{id}")
class DeleteAuthorTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 with an empty body for an existing author")
    void deletesExistingAuthor() {
        assertThatResponse(authorsClient.delete(1))
                .hasStatusCode(200)
                .hasEmptyBody();
    }

    @ParameterizedTest(name = "id \"{0}\" returns 400")
    @ValueSource(strings = {"abc", "1.5"})
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an id that is not an integer")
    void rejectsMalformedId(String malformedId) {
        assertThatResponse(authorsClient.delete(malformedId)).hasStatusCode(400);
    }
}
