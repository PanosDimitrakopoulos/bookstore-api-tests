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
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.bookstore.api.assertion.ModelAssert.assertSameModel;
import static com.bookstore.api.assertion.ResponseAssert.assertThatResponse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Epic(ReportLabels.EPIC)
@Feature(ReportLabels.AUTHORS)
@Story("Create an author")
@DisplayName("POST /api/v1/Authors")
class CreateAuthorTest {

    private final AuthorsClient authorsClient = new AuthorsClient();

    @Test
    @Tag(TestTags.SMOKE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("returns 200 and echoes back the created author")
    void createsAuthor() {
        Author newAuthor = AuthorTestData.validAuthor();

        Author created = assertThatResponse(authorsClient.create(newAuthor))
                .hasStatusCode(200)
                .hasJsonContentType()
                .matchesJsonSchema(JsonSchemas.AUTHOR)
                .extractingBodyAs(Author.class);

        assertSameModel(created, newAuthor);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("specialNameAuthors")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("accepts names with special characters")
    void acceptsSpecialCharacterNames(Author author) {
        Author created = assertThatResponse(authorsClient.create(author))
                .hasStatusCode(200)
                .extractingBodyAs(Author.class);

        assertSameModel(created, author);
    }

    static Stream<Arguments> specialNameAuthors() {
        Author base = AuthorTestData.validAuthor();
        return Stream.of(
                arguments(Named.of("Greek name", base.withFirstName("Νίκος").withLastName("Καζαντζάκης"))),
                arguments(Named.of("apostrophe and hyphen", base.withFirstName("Mary-Jane").withLastName("O'Connor"))),
                arguments(Named.of("diacritics", base.withFirstName("José").withLastName("Müller-Lüdenscheidt"))));
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("returns 400 for malformed JSON")
    void rejectsMalformedJson() {
        assertThatResponse(authorsClient.createRaw("{\"firstName\": ", ContentType.JSON)).hasStatusCode(400);
    }

    @Test
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 for an empty request body")
    void rejectsEmptyBody() {
        assertThatResponse(authorsClient.createRaw("", ContentType.JSON)).hasStatusCode(400);
    }

    @ParameterizedTest(name = "{0} = {1}")
    @MethodSource("invalidFieldTypes")
    @Tag(TestTags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("returns 400 when a numeric field has the wrong type")
    void rejectsInvalidFieldTypes(String field, Object invalidValue) {
        Map<String, Object> payload = AuthorTestData.validAuthorAsMap();
        payload.put(field, invalidValue);

        assertThatResponse(authorsClient.create(payload)).hasStatusCode(400);
    }

    static Stream<Arguments> invalidFieldTypes() {
        return Stream.of(
                arguments("id", "abc"),
                arguments("idBook", "first book"),
                arguments("idBook", 2_147_483_648L));
    }
}
