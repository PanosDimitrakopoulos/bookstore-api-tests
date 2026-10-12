package com.bookstore.tests.authors;

import com.bookstore.api.client.AuthorsClient;
import com.bookstore.api.data.AuthorTestData;
import com.bookstore.api.model.Author;
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
@Feature(ReportLabels.AUTHORS)
@Story("Known API defects")
@DisplayName("Authors API: known defects")
class AuthorsKnownDefectsTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("a created author should be retrievable by its id")
    @Description("The API echoes the new author but does not store it, so a follow-up GET returns 404.")
    void createdAuthorShouldBePersisted() {
        Author newAuthor = AuthorTestData.validAuthor();
        assertThatResponse(authorsClient.create(newAuthor)).hasStatusCode(200);

        Response lookup = authorsClient.getById(newAuthor.id());

        assertThat(lookup.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "GET /Authors/%d right after creating it should return 200", newAuthor.id())
                .isEqualTo(200);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("POST should reject an author linked to a non-existent book")
    @Description("idBook is not checked for referential integrity; any integer is accepted.")
    void createWithUnknownBookShouldBeRejected() {
        Response response = authorsClient.create(AuthorTestData.validAuthor().withIdBook(999_999));

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "POST /Authors with idBook 999999 should return 400 or 422")
                .isIn(400, 422);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DELETE of a non-existent author should return 404")
    @Description("The API answers 200 for any integer id.")
    void deleteUnknownAuthorShouldReturnNotFound() {
        Response response = authorsClient.delete(999_999);

        assertThat(response.statusCode())
                .as(KNOWN_DEFECT_PREFIX + "DELETE /Authors/999999 should return 404")
                .isEqualTo(404);
    }
}
