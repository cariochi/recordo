# Introduction

**Recordo** is a JUnit 5 extension that takes the pain out of dealing with test data and mock interactions. Instead of hard-coding DTOs, JSON strings or HTTP stubs in your tests, Recordo moves these resources into files and generates or records them when they don’t yet exist. On the first run, if the required files don’t exist, Recordo generates them (for objects or captured HTTP interactions). You review or adjust these files, and on subsequent runs Recordo loads or replays them as fixtures, making tests deterministic.

Key benefits at a glance:

* **Read Module** – annotate test parameters or fields with `@Read` to automatically load JSON, CSV, or ZIP resources into objects. Missing files are generated automatically, ensuring fast deterministic tests.
* **Assertions Module** – assert JSON or CSV responses with expressive fluent APIs. Compare against expected files, filter fields, or ignore order with clear options.
* **MockMvc Module** – build declarative HTTP clients for your controllers in tests. Use familiar Spring MVC annotations (`@GetMapping`, `@PostMapping`, etc.) and Recordo will generate typed clients, handle requests, and record responses.
* **MockServer Module** – capture and replay HTTP traffic from real clients like `OkHttp` or `RestTemplate`. Save interactions to JSON files and replay them in tests for stable, reproducible scenarios.

Recordo combines these modules into a single coherent testing toolkit. It is especially useful when working with complex DTOs, deeply nested JSON structures, or external HTTP APIs, reducing boilerplate and making tests both **readable** and **maintainable**.

👉 Source code is available on [GitHub](https://github.com/cariochi/recordo).

# Getting Started

## Maven Dependencies

Recordo is modular. You can either pull in **all modules** for convenience, or declare **only the ones you need**. This gives you flexibility to keep your build lean.

### All Modules

Use the `recordo-all` artifact to include everything:

```xml
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-all</artifactId>
    <version>2.0.9</version>
    <type>pom</type>
    <scope>test</scope>
</dependency>
```

### Individual Modules

If you want to be explicit and import only what you actually use:

```xml
<!-- Read Module -->
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-read</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>

<!-- Assertions Module -->
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-assertions</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>

<!-- MockMvc Module -->
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-spring-mockmvc</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>

<!-- MockServer Module -->
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-mockserver</artifactId>
    <version>2.0.9</version>
    <scope>test</scope>
</dependency>
```

## Compatibility

* **1.x.x** — Java 11+ and Spring 5.x (Spring Boot 2.x)
* **2.x.x** — Java 17+ and Spring 6.x (Spring Boot 3.x)

Note: the modules `recordo-read`, `recordo-assertions`, and `recordo-mockserver` work perfectly in projects **without Spring**. Only `recordo-spring-mockmvc` requires a Spring context.

## Initialize Extension

Register the JUnit 5 extension in tests:

```java
@ExtendWith(RecordoExtension.class)
class MyTest {
    // ...
}
```

## ObjectMapper

Recordo uses Jackson `ObjectMapper` for JSON serialization and deserialization. The resolution order is:

1. If a test class has a field annotated with `@EnableRecordo`, that `ObjectMapper` instance will be used.
2. If no such field is present and you run under Spring, Recordo will look for a single `ObjectMapper` bean in the application context.
3. If neither is found, a built‑in default mapper is used.

You can register multiple mappers and reference them by name in annotations (e.g., `@Read(objectMapper = "customMapper")`).

## Additional Properties

You can fine-tune behavior in `recordo.properties` on the test classpath:

**`recordo.resources.root`**
Overrides the base directory where Recordo **reads** expected files and **writes** generated/recorded ones.
*Default:* `src/test/resources/`

Examples:

```properties
recordo.resources.root=src/it/resources
```
**`recordo.http.headers.included`**
Comma‑separated list of header names that must be included into recordings and used for matching during replay.

**`recordo.http.headers.sensitive`**
Comma‑separated list of **sensitive** headers that should be masked in stored recordings (e.g., `Authorization`).

Examples:

```properties
recordo.http.headers.included=Accept,Content-Type,Authorization
recordo.http.headers.sensitive=Authorization,Set-Cookie
```

# Read Module

The Read module lets you source test objects from external files and keep them under version control. If a referenced file is missing on the first run, Recordo generates it with a sensible structure and randomized values; on subsequent runs the same file is read back, keeping your tests deterministic.

## Annotation Parameters

`@Read` supports:

* **`value`** – resource path (e.g. `/books/book.json`).
* **`objectMapper`** – optional name of an `ObjectMapper` bean or test‑class field to use for (de)serialization.

## Approach 1 — Interface‑based Object Factory

Declare a factory interface with `@RecordoObjectFactory`. Mark factory methods with `@Read` to bind them to files. Use `@Modifier` methods for fluent tweaks.

```java
@RecordoObjectFactory
public interface LogRecordFactory {

    // one object
    @Read("/messages/log.json")
    LogRecord logRecord();

    // list of objects
    @Read("/messages/logs.json")
    List<LogRecord> list();

    // raw bytes (e.g., zip archive)
    @Read("/files/out.zip")
    byte[] archive();

    // fluent modifications
    @Modifier("id")
    LogRecordFactory withId(long id);

    @Modifier("responses[0].status")
    LogRecordFactory withFirstStatus(Status status);
}
```

**Usage**

```java
@ExtendWith(RecordoExtension.class)
class LogTests {
    
    LogRecordFactory factory = Recordo.create(LogRecordFactory.class);

    @Test
    void creates_and_modifies() {
        LogRecord base = factory.logRecord();
        LogRecord modified = factory.withId(123).withFirstStatus(Status.SUCCESS).logRecord();
        byte[] zip = factory.archive();
    }
}
```

## Approach 2 — Direct Object Creation

Use `@Read` directly on test parameters for quick loading without factories.

```java
@Test
void single_object(@Read("/books/book.json") Book book) { /* ... */ }

@Test
void list_of_objects(@Read("/books/books.json") List<Book> books) { /* ... */ }

@Test
void as_string(@Read("/texts/info.txt") String text) { /* ... */ }

@Test
void as_bytes(@Read("/files/archive.zip") byte[] bytes) { /* ... */ }
```

## Supported Types

* POJOs/DTOs via Jackson
* `List<T>` / arrays
* `String`
* `byte[]`

## First‑Run Behavior

* If the target file does not exist, Recordo **creates** it with a generated example that matches the return type.
* You may edit the file to your needs; subsequent runs **read** it as a fixture.

> Tip: prefer small, focused files per test scenario; this keeps diffs readable and reviewable.

# Assertions Module

The Assertions module compares objects and strings against JSON/CSV files using three classes:

* `JsonAssertion` — object ⇄ JSON file comparison.
* `JsonCondition` — AssertJ `Condition` for object ⇄ JSON file comparison.
* `CsvAssertion` — CSV string ⇄ CSV file comparison.


## JSON Assertions (`JsonAssertion`)

Pass any object (e.g., DTOs, collections, pages). Recordo will serialize it via Jackson and compare with the expected JSON file.

```java
import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@Test
void shouldMatchBooksShortView() {
    Page<Book> books = bookService.findAllByAuthor(author);

    assertAsJson(books)
        .including("content[*].id", "content[*].title", "content[*].author.id")
        .isEqualTo("/books/short_books.json");
}
```

### Options

* `.including(paths...)` — compare only the listed JSON paths.
* `.excluding(paths...)` — ignore listed paths.
* `.extensible(true)` — allow extra fields.
* `.withStrictOrder(true)` — require array order to match.
* `.using(ObjectMapper)` — custom mapper.


## JSON Assertions as AssertJ Condition (`JsonCondition`)

`JsonCondition` provides an alternative syntax for the same functionality. It supports the same options as `JsonAssertion`.

```java
import static com.cariochi.recordo.assertions.JsonCondition.equalAsJsonTo;
import static org.assertj.core.api.Assertions.assertThat;

@Test
void shouldMatchBooksShortView_withCondition() {
    Page<Book> books = bookService.findAllByAuthor(author);

    assertThat(books)
        .is(equalAsJsonTo("/books/short_books.json")
            .including("content[*].id", "content[*].title", "content[*].author.id")
        );
}
```


## CSV Assertions (`CsvAssertion`)

Compare an **actual CSV string** with an expected CSV file.

```java
import static com.cariochi.recordo.assertions.CsvAssertion.assertCsv;

@Test
void shouldMatchCsv() {
    String actualCsv = "id,name\n1,John\n";

    assertCsv(actualCsv)
        .withHeaders(true)
        .withStrictOrder(true)
        .withColumnSeparator(';')
        .withLineSeparator("\r\n")
        .isEqualsTo("/expected/users.csv");
}
```

### Options

* `.withHeaders(boolean)` — treat the first row as a header.
* `.withStrictOrder(boolean)` — require rows to appear in the same order.
* `.withColumnSeparator(char)` — set a custom column separator (default is comma).
* `.withLineSeparator(String)` — set a custom line separator.


## First Run & Debugging Failures

* **First run (or if the expected file was deleted)**: Recordo serializes the actual data (object or CSV string) and creates the corresponding JSON/CSV file automatically.
* **When a comparison fails**: Recordo writes the actual data into an `ACTUAL/` subfolder next to the expected file, using the same file name as in `isEqualTo`. For example, `/books/short_books.json` will produce `/books/ACTUAL/short_books.json`. This allows you to diff expected vs actual in your IDE. The `ACTUAL/` folder is only for debugging — delete it after review and never commit it to version control.

## Recommended .gitignore additions

```
# Recordo temporary outputs
**/ACTUAL/
```

# MockMvc Module

This module lets you call Spring MVC controllers via **type‑safe clients** defined as annotated interfaces and executed with Spring `MockMvc`.

⚠️ Recordo will work only if there is a **single `MockMvc` instance** available in the Spring context.


## Define the client interface

```java
@RecordoApiClient(
    objectMapper = "customMapper",                                      // bean name (optional)
    interceptors = { LocaleInterceptor.class, AuthInterceptor.class }   // optional
)
@RequestMapping("/users")
interface UserApiClient {

    @GetMapping("/{id}")
    UserDto findById(@PathVariable("id") Long id);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserDto create(@RequestBody UserDto user);

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable("id") Long id);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorDto getById_withErrors(@PathVariable int id,
                                @RequestParam("name") String name,
                                @RequestHeader("Authorization") String auth);
}
```

**Notes**

* In `@RecordoApiClient` interfaces you use **standard Spring annotations**: `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@RequestHeader`, `@ResponseStatus`.
* Recordo executes requests via `MockMvc`, automatically verifies the HTTP status, and maps the response body into the declared return type.
* The expected status is declared with `@ResponseStatus`. This allows you to write not only positive tests but also **error handling** or **security tests**.


## Create and use the client

```java
@WebMvcTest(UserController.class)
@ExtendWith(RecordoExtension.class)
class UserControllerTest {
    
    private final UserApiClient api = Recordo.create(UserApiClient.class);

    @Test
    void shouldCreateAndFetch() {
        UserDto created = api.create(new UserDto("John"));   // 201 CREATED (from @ResponseStatus)
        UserDto loaded  = api.findById(created.getId());     // 200 OK (default)
    }
}
```


## Return types

Recordo MockMvc clients support three styles of return values:

**Direct result**

```java
// Declaration
@GetMapping("/{id}")
UserDto getById(@PathVariable int id, @RequestParam("name") String name);

// Usage
UserDto user = apiClient.getById(1, "Test User");
```

**Response wrapper**

```java
// Declaration
@GetMapping("/{id}")
Response<UserDto> getById(@PathVariable int id, @RequestParam("name") String name);

// Usage
Response<UserDto> response = apiClient.getById(1, "Test User");
UserDto userDto = response.getBody();
Map<String, String> headers = response.getHeaders();
HttpStatus status = response.getStatus();
```

**Request object**

```java
// Declaration
@GetMapping("/{id}")
Request<UserDto> getById(@PathVariable int id, @RequestParam("name") String name);

// Usage
Request<UserDto> request = apiClient.getById(1, "Test User");
Response<UserDto> response = request.header("Authorization", "Bearer ...").perform();
```


## Request interceptors

Declare interceptors in `@RecordoApiClient(interceptors = {...})`. Resolution order:

1. if a Spring bean of that type exists in the context — it is used;
2. otherwise, the interceptor is **instantiated via default constructor**.

Interceptors can mutate outgoing requests (e.g., add headers) before they are executed by `MockMvc`.

**Example:**

```java
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements RequestInterceptor {

    private static SecurityService securityService;
    
    @Override
    public Request<?> apply(Request<?> request) {
        if (request.headers().get("Authorization") == null) {
            request = request.header("Authorization", "Bearer " + securityService.currentToken());
        }
        return request;
    }
}
```
# MockServer Module

Recordo can **record** real HTTP interactions on the first run and **replay** them on subsequent runs. Interactions are persisted as JSON and kept under version control, so your tests remain deterministic.

## How it works on test runs

* **First run (or if the file is missing)** — real HTTP calls are executed; Recordo captures request/response pairs and writes them to the configured file/folder.
* **Subsequent runs** — HTTP calls are **not** sent to the network; responses are taken from the saved JSON.

This module keeps your integration tests fast, repeatable, and reviewable (recordings are plain JSON files).


## Setup with Spring

### Interceptor-first integration (primary)

Recordo integrates via a **dedicated HTTP interceptor bean** that you attach to your HTTP client. The main idea: **create an interceptor bean and add it to the client**, so that Recordo can intercept requests/responses.

Available interceptors:

* `ApacheRecordoInterceptor`
* `RestClientRecordoInterceptor`
* `RestTemplateRecordoInterceptor`
* `OkhttpRecordoInterceptor`

> Make the interceptor a Spring **bean** and wire it into the client you use.

**OkHttp (OkHttpClient)**

```java
@Bean
OkhttpRecordoInterceptor recordoOkhttpInterceptor() { return new OkhttpRecordoInterceptor(); }

@Bean
OkHttpClient okHttpClient(OkhttpRecordoInterceptor interceptor) {
    return new OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build();
}
```

**Apache HttpClient (client5)**

```java
@Bean
ApacheRecordoInterceptor recordoApacheInterceptor() { return new ApacheRecordoInterceptor(); }

@Bean
org.apache.hc.client5.http.classic.HttpClient apacheHttpClient(ApacheRecordoInterceptor interceptor) {
    return HttpClients.custom()
        .addRequestInterceptorFirst(interceptor)
        .build();
}
```

**Spring RestTemplate**

```java
@Bean
RestTemplateRecordoInterceptor recordoRestTemplateInterceptor() { return new RestTemplateRecordoInterceptor(); }

@Bean
RestTemplate restTemplate(RestTemplateRecordoInterceptor interceptor) {
    var restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(interceptor);
    return restTemplate;
}
```

**Spring RestClient**

```java
@Bean
RestClientRecordoInterceptor recordoRestClientInterceptor() { return new RestClientRecordoInterceptor(); }

@Bean
RestClient restClient(RestClientRecordoInterceptor interceptor) {
    return RestClient.builder()
        .requestInterceptor(interceptor)
        .build();
}
```

### Auto-wiring client bean (alternative)

As an alternative, Recordo can **search for an HTTP client bean** in the Spring context and automatically attach its interceptor. This requires no explicit interceptor wiring, but works only when there is a **single supported client bean** of that type in the context.

Example with `RestTemplate`:

```java
@SpringBootTest
class BookServiceTest {

    @Autowired
    private RestTemplate restTemplate; // Recordo will detect and wrap it automatically

    @Test
    @MockServer("/mock_servers/books.json")
    void should_retrieve_books() {
        List<Book> books = bookClient.getBooks();
        // interactions recorded or replayed automatically
    }
}
```

## Using Recordo without Spring

Recordo can also be used in projects **without Spring**. In this case you annotate the HTTP client or interceptor field with `@EnableRecordo`, and Recordo will instrument it directly.

### With explicit interceptor

```java
@ExtendWith(RecordoExtension.class)
class GitHubServiceTest {

    @EnableRecordo
    private final OkhttpRecordoInterceptor recordoInterceptor = new OkhttpRecordoInterceptor();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(recordoInterceptor)
            .build();

    private final GitHubService service = new GitHubService(client);

    @Test
    @MockServer("/mockserver/gists.mock.json")
    void test_mock_server() {
        List<GistDto> gists = service.getGists();
        // first run records, next runs replay
    }
}
```

### Without explicit interceptor

```java
@ExtendWith(RecordoExtension.class)
class GitHubServiceTest {

    @EnableRecordo
    private final OkHttpClient client = new OkHttpClient();

    private final GitHubService service = new GitHubService(client);

    @Test
    @MockServer("/mockserver/gists.mock.json")
    void test_mock_server() {
        List<GistDto> gists = service.getGists();
        // first run records, next runs replay
    }
}
```

In both cases, `@EnableRecordo` tells Recordo to wrap the client or interceptor so that interactions can be recorded and replayed during tests.


## OpenFeign (via supported HTTP clients)

OpenFeign can be used together with Recordo by wiring it to one of the **supported HTTP clients** (OkHttp or Apache HttpClient). Recordo will attach its interceptors to the underlying client and capture/replay calls.

**OkHttp setup**

```java
@Bean
public okhttp3.OkHttpClient okHttpClient() {
    return new okhttp3.OkHttpClient();
}

@Bean
public feign.Client feignClient(okhttp3.OkHttpClient okHttpClient) {
    return new feign.okhttp.OkHttpClient(okHttpClient);
}
```

**Apache HttpClient setup**

```java
@Bean
public org.apache.hc.client5.http.classic.HttpClient apacheHttpClient() {
    return HttpClients.createDefault();
}

@Bean
public feign.Client feignClient(org.apache.hc.client5.http.classic.HttpClient httpClient) {
    return new feign.httpclient.ApacheHttpClient(httpClient);
}
```

> Note: Recordo interacts with Feign **through** the configured OkHttp/Apache client by inserting its interceptors. Make sure these clients are the ones used by Feign in the Spring context.


## Annotation Parameters

Use `@MockServer` on a test method (or class). Parameters:

* **`value`** (String) — path to the storage location:
    * **File path** ending with `.json` → *all interactions* are recorded into a **single file**.
    * **Folder path** (no `.json`) → each interaction is recorded as a **separate JSON file** inside the folder.


* **`urlPattern`** (String) — optional URL matcher; supports `?` (one char), `*` (zero or more chars), `**` (zero or more path segments).


* **`beanName`** (String) — bean name, which can be either the name of the interceptor bean (in interceptor-first mode) or the client bean (in auto-wiring mode).


* **`objectMapper`** (String) — name of `ObjectMapper` bean or test field.


## Examples

### Single server (file storage)

```java
@Test
@MockServer("/mock_servers/get_gists.json")
void should_retrieve_gists() {
    List<GistResponse> gists = gitHubClient.getGists();
    // first run records, next runs replay
}
```

### Single server (folder storage)

```java
@Test
@MockServer("/mock_servers/gists/") // folder
void should_retrieve_gists_in_folder_mode() {
    List<GistResponse> gists = gitHubClient.getGists();
}
```

### Multiple HTTP clients

```java
@Autowired private RestTemplate bookServerRestTemplate;
@Autowired private RestTemplate authorServerRestTemplate;

@Test
@MockServer(httpClient = "bookServerRestTemplate", value = "/mockserver/multiservers/books-server.rest.json")
@MockServer(httpClient = "authorServerRestTemplate", value = "/mockserver/multiservers/authors-server.rest.json")
void should_retrieve_books_and_authors() {
    // calls using two different clients are recorded into two files
}
```

### Multiple servers (URL pattern)

```java
@Test
@MockServer(beanName = "https://books.server/**",  value = "/mockserver/multiservers/books-server.rest.json")
@MockServer(beanName = "https://authors.server/**", value = "/mockserver/multiservers/authors-server.rest.json")
void should_retrieve_from_multiple_servers() {
    // calls to matching hosts go into the respective files
}
```

# License

Recordo is distributed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
