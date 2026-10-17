# Bookstore API Tests

Automated API tests for the [FakeRestAPI](https://fakerestapi.azurewebsites.net/index.html) online bookstore.
The suite tests the **Books** and **Authors** endpoints (happy paths, edge cases and JSON-schema contract checks),
produces Allure reports and runs in GitHub Actions.

| | |
|---|---|
| Language | Java 17 |
| Build | Maven |
| HTTP client | REST Assured 5 |
| Test runner | JUnit 5 (parameterized tests, tags, optional parallel classes) |
| Assertions | AssertJ + custom fluent `ResponseAssert` |
| Contract checks | JSON Schema (REST Assured `json-schema-validator`) |
| Test data | Datafaker (randomized, realistic payloads) |
| Reporting | Allure 2 (full HTML + single-file HTML), Surefire XML |
| CI | GitHub Actions |

---

## Quick start

**Prerequisites:** JDK 17+, Maven 3.9+. Node.js 18+ is only needed to build the Allure report locally.

```bash
git clone https://github.com/PanosDimitrakopoulos/bookstore-api-tests.git
cd bookstore-api-tests

# Run the regression suite (everything except tests that document known API defects)
mvn test

# Open the Allure report in your browser
npx allure-commandline@2.46.1 serve target/allure-results
```

A successful run ends with:

```
[INFO] Tests run: 82, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The tests call the public demo API, so an internet connection is required.

---

## Project structure

```
bookstore-api-tests
├── .github/workflows/api-tests.yml       CI pipeline
├── pom.xml                               dependencies, Surefire config, test profiles
└── src
    ├── main                              reusable framework (no test cases)
    │   ├── java/com/bookstore/api
    │   │   ├── config/ApiConfig          configuration: system property > env var > config.properties
    │   │   ├── http/                     shared request spec (base URI, timeouts, JSON mapper, Allure filter)
    │   │   ├── client/                   ResourceClient (generic CRUD) + BooksClient, AuthorsClient
    │   │   ├── model/                    Book, Author (immutable records)
    │   │   ├── data/                     BookTestData, AuthorTestData (randomized payloads)
    │   │   └── assertion/                ResponseAssert, ModelAssert, JsonSchemas
    │   └── resources
    │       ├── config.properties         default environment settings
    │       └── schemas/                  JSON schemas for Book, Author and their lists
    └── test
        ├── java/com/bookstore
        │   ├── support/                  tags, report labels, Allure metadata listener
        │   └── tests
        │       ├── books/                one test class per endpoint + BooksKnownDefectsTest
        │       └── authors/              one test class per endpoint + AuthorsKnownDefectsTest
        └── resources                     JUnit parallel config, Allure categories, logging
```

The framework code in `src/main` builds and sends requests. Test classes in `src/test` call it with each case's
input and assert the expected result, without building HTTP requests themselves.

### Design decisions

- **Single responsibility.** Each class does one thing: `ApiConfig` resolves settings, `RequestSpecFactory`
  builds the HTTP baseline, clients send requests, `ResponseAssert` verifies responses, `*TestData` builds payloads.
- **Open/closed.** `ResourceClient` implements the five CRUD calls once. A new resource is a few-line subclass
  that only supplies its path. `AuthorsClient` adds its extra `/authors/books/{idBook}` lookup without touching the base.
- **Dependency inversion.** Clients receive their `RequestSpecification` through the constructor (with a default
  for convenience), so the base URL, headers or filters can be swapped without changing client or test code.
- **Clients return raw responses.** Each client method returns the REST Assured `Response` and the test decides
  what to check, so the same methods serve `200` happy paths and `4xx` error cases. Ids and payloads are typed as
  `Object` so negative tests can send `"abc"` as an id or a map with a wrongly typed field.
- **Immutable models.** `Book` and `Author` are records with `withX` methods, so a boundary case is one line:
  `validBook().withPageCount(0)`.
- **No shared state between tests.** Every test builds its own data, so test classes can run in parallel safely.
  Parallel execution is off by default to go easy on the shared demo host; see [Running tests](#running-tests).
- **Readable failures.** Status-code failures include the response body, and every request/response is attached
  to the Allure report, so a failure can be triaged from the report without re-running anything.

---

## Running tests

| Goal | Command |
|---|---|
| Regression suite (default, used to gate CI) | `mvn test` |
| Smoke tests only | `mvn test -Dgroups=smoke` |
| Negative tests only | `mvn test -Dgroups=negative` |
| Contract (schema) tests only | `mvn test -Dgroups=contract` |
| Known-defect tests only (expected to fail) | `mvn test -Pknown-defects` |
| Everything | `mvn test -Pall-tests` |
| One class | `mvn test -Dtest=CreateBookTest` |
| One method | `mvn test -Dtest=CreateBookTest#createsBook` |
| Retry failures once (flaky network) | `mvn test -Dsurefire.rerunFailingTestsCount=1` |
| Run test classes in parallel (4 threads) | `mvn test -Djunit.jupiter.execution.parallel.enabled=true` |

### Configuration

Defaults live in `src/main/resources/config.properties`. Any value can be overridden without editing files.
The first match wins:

1. JVM system property: `mvn test -Dapi.base.url=https://staging.example.com`
2. Environment variable (upper case, dots → underscores):
   - bash / zsh: `API_BASE_URL=https://staging.example.com mvn test`
   - PowerShell: `$env:API_BASE_URL="https://staging.example.com"; mvn test`
   - cmd: `set "API_BASE_URL=https://staging.example.com" && mvn test`
3. `config.properties`

| Key | Default | Purpose |
|---|---|---|
| `api.base.url` | `https://fakerestapi.azurewebsites.net` | Host under test |
| `api.base.path` | `/api/v1` | API version prefix |
| `api.connect.timeout.ms` | `10000` | Connection timeout |
| `api.read.timeout.ms` | `30000` | Socket read timeout |
| `api.max.response.time.ms` | `5000` | Limit used by the response-time check |
| `api.http.logging.enabled` | `false` | Print requests/responses to the console |

---

## Test reports

Each run writes Allure results to `target/allure-results` and JUnit XML to `target/surefire-reports`.

```bash
# Interactive report served locally
npx allure-commandline@2.46.1 serve target/allure-results

# Static report
npx allure-commandline@2.46.1 generate target/allure-results --clean -o target/allure-report

# Single self-contained HTML file (easy to share or attach)
npx allure-commandline@2.46.1 generate target/allure-results --clean --single-file -o target/allure-report-single
```

The report shows pass/fail per test case, grouped by endpoint (Behaviors tab), with the full request and
response attached to every test. Failures are sorted into categories:

- **Known API defects (expected failures)**: tests tagged `known-defect`, see below
- **Unexpected API failures**: any other failed assertion
- **Connectivity / environment issues**: timeouts, refused connections, DNS errors
- **Test code errors**: exceptions in the test code itself

The Overview page lists the environment the run targeted (API URL, Java version, OS).

---

## CI/CD pipeline

`.github/workflows/api-tests.yml` runs on every push to `main`, on pull requests, daily at 05:17 UTC
(the API is external and can change without a commit here), and manually from the Actions tab
(with optional base URL and tag filter).

Pipeline steps:

1. Set up JDK 17 with Maven dependency caching.
2. **Run the regression suite.** This gates the build; failed tests are retried once to absorb transient
   errors from the shared demo host.
3. **Run the known-defect tests** without failing the build, so their results join the same report.
4. Publish a pass/fail summary of every test case to the workflow run page.
5. Generate the Allure report (full and single-file) and upload them as build artifacts
   (`allure-report-single-file`, `allure-report-full`), kept for 30 days.
6. Optionally deploy the report to GitHub Pages: set the repository variable `PUBLISH_REPORT_TO_PAGES=true`
   and choose **GitHub Actions** as the Pages source in the repository settings.

To get the report from a run: **Actions → API Tests → (a run) → Artifacts → `allure-report-single-file`**,
then open `index.html` in any browser.

---

## Test coverage

**Books** (`/api/v1/Books`)

| Endpoint | Happy paths | Edge and negative cases |
|---|---|---|
| `GET /Books` | 200, non-empty list, schema, unique positive ids, titles present, response time | None: the endpoint takes no input |
| `GET /Books/{id}` | 200 for several ids, schema, consistent with list endpoint | 404 for `0`, `-1`, `999999`, `Int32.MaxValue`; 400 for `abc`, `1.5`, `1e3`, Int32 overflow |
| `POST /Books` | 200 + echoed body, schema | Boundaries: page count `0` and Int32 max, id Int32 max, 1-char and 1,000-char titles, Unicode/emoji, empty optional fields, dates in 1900 and 2100. Rejections: malformed JSON, empty body, array body, wrong field types, Int32 overflow, invalid date, `text/plain` (415) |
| `PUT /Books/{id}` | 200 + echoed body, schema, single-field change | 400 for malformed id, malformed JSON, empty body, wrong field type |
| `DELETE /Books/{id}` | 200 + empty body | 400 for malformed id and Int32 overflow |

**Authors** (`/api/v1/Authors`, bonus)

| Endpoint | Happy paths | Edge and negative cases |
|---|---|---|
| `GET /Authors` | 200, non-empty list, schema, unique ids, every author linked to a book | None: the endpoint takes no input |
| `GET /Authors/{id}` | 200, schema, consistent with list endpoint | 404 for unknown ids, 400 for malformed ids |
| `GET /Authors/authors/books/{idBook}` | Only authors of the requested book, schema | Empty list for an unknown book, 400 for malformed id |
| `POST /Authors` | 200 + echoed body, schema, Greek / diacritics / apostrophe names | 400 for malformed JSON, empty body, wrong field types |
| `PUT /Authors/{id}` | 200 + echoed body, schema | 400 for malformed id and malformed JSON |
| `DELETE /Authors/{id}` | 200 + empty body | 400 for malformed ids |

Counting each parameterized case separately, there are 82 regression test cases and 10 known-defect tests.

---

## Known API behaviour and defects

FakeRestAPI is a stateless demo service: it returns generated data, echoes writes back without storing them,
and doesn't follow some REST conventions. The suite handles this with two kinds of test:

- **Regression tests assert what the API actually does** (for example, `POST` returns `200` and echoes the
  body). The gating run passes against the demo, so a failing build points to a change in API behaviour
  or to a connectivity problem, which the report lists separately.
- **Known-defect tests assert what a real bookstore API should do** and fail against the demo by design.
  They are tagged `known-defect` and excluded from the gating run. CI runs them in a separate step, and the
  report lists them under "Known API defects" with a description of each issue.

| # | Expected behaviour | Actual behaviour |
|---|---|---|
| 1 | `POST` returns `201 Created` | `200 OK` |
| 2 | A created book/author can be fetched by its id | `404`: nothing is persisted |
| 3 | A deleted book can no longer be fetched | Still returned with `200` |
| 4 | `DELETE` of an unknown id returns `404` | `200` |
| 5 | `PUT` of an unknown id returns `404` | `200` with the body echoed |
| 6 | `PUT` rejects a body id that differs from the path id | Accepted |
| 7 | `POST` rejects a book with an empty title | Accepted, no validation |
| 8 | `POST /Authors` rejects an `idBook` that doesn't exist | Accepted, no referential check |

The demo generates `publishDate` relative to the current time, so tests that compare two calls ignore that field.

---

## Extending the suite

Adding a new resource, e.g. `/api/v1/Users`:

1. Add a `User` record in `model/`.
2. Add `UsersClient extends ResourceClient` with the path `/Users`; all CRUD calls are inherited.
3. Add `UserTestData` in `data/` and a schema in `resources/schemas/` (register it in `JsonSchemas`).
4. Add test classes under `src/test/java/com/bookstore/tests/users/`, one per endpoint, using
   `assertThatResponse(...)` and the existing tags.

---

## Troubleshooting

- **Tests fail with timeouts or `5xx`.** The demo host is shared and occasionally slow. Re-run, or use
  `-Dsurefire.rerunFailingTestsCount=1`. Timeouts appear under "Connectivity / environment issues" in the report;
  a `5xx` response is a status-code mismatch, so it appears under "Unexpected API failures".
- **Response-time test fails.** Raise the limit: `-Dapi.max.response.time.ms=10000`.
- **See raw traffic in the console.** `mvn test -Dapi.http.logging.enabled=true`.
