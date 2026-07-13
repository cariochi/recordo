# Recordo

Recordo is a JUnit 5 testing toolkit for Java projects. It moves test fixtures, expected JSON/CSV documents, MockMvc
calls, and recorded HTTP interactions out
of test code and into versioned resource files.

The typical workflow is:

1. Write a test that references a resource file.
2. Run the test once.
3. If the file does not exist, Recordo creates it from the actual object, generated object, CSV content, or captured
   HTTP traffic.
4. Review and adjust the generated file.
5. Commit the fixture and keep future test runs deterministic.

Recordo is useful when tests work with large DTOs, nested JSON, Spring MVC controllers, or external HTTP APIs that
should be recorded once and replayed later.

# When To Use Recordo

Recordo fits Java projects where tests are easier to understand and maintain when fixtures, expected outputs, and
recorded interactions live in files instead
of test code.

It is especially useful for:

- Spring Boot services with MockMvc controller tests.
- REST clients that call external HTTP APIs.
- Tests with large DTOs, nested JSON, or long expected responses.
- Approval-style workflows where generated `ACTUAL/` files are reviewed before becoming expected fixtures.
- Teams that want stable tests without manually writing every JSON fixture or HTTP stub.

# Modules

Recordo has four user-facing modules:

| Module         | Artifact                 | Purpose                                                             |
|----------------|--------------------------|---------------------------------------------------------------------|
| Read           | `recordo-read`           | Load objects, collections, strings, bytes, or factories from files. |
| Assertions     | `recordo-assertions`     | Compare actual values with expected JSON or CSV files.              |
| Spring MockMvc | `recordo-spring-mockmvc` | Build typed API clients and requests on top of Spring `MockMvc`.    |
| MockServer     | `recordo-mockserver`     | Record and replay HTTP traffic for supported clients.               |

# Requirements

Recordo `2.1.x` is built for:

- Java 17 or newer.
- JUnit Jupiter.
- Jackson 3 for JSON serialization, deserialization, assertions, and recorded HTTP fixtures.
- Spring Boot 4 / Spring Framework 7 and the Jakarta stack for Spring-specific modules.

The `recordo-read`, `recordo-assertions`, and `recordo-mockserver` modules can be used without Spring. The
`recordo-spring-mockmvc` module requires Spring MVC
and Spring Test.

For Spring Boot 3 / Spring Framework 6 projects, use Recordo `2.0.x`: [Recordo v2.0.x]({{ '/archive/recordo-v2.0.x' | relative_url }}).

# Installation

Use `recordo-all` if you want all four modules in one dependency:

```xml

<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-all</artifactId>
    <version>2.1.1</version>
    <type>pom</type>
    <scope>test</scope>
</dependency>
```

Or add only the modules you need:

```xml

<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-read</artifactId>
    <version>2.1.1</version>
    <scope>test</scope>
</dependency>

<dependency>
   <groupId>com.cariochi.recordo</groupId>
   <artifactId>recordo-assertions</artifactId>
   <version>2.1.1</version>
   <scope>test</scope>
</dependency>

<dependency>
   <groupId>com.cariochi.recordo</groupId>
   <artifactId>recordo-spring-mockmvc</artifactId>
   <version>2.1.1</version>
   <scope>test</scope>
</dependency>

<dependency>
   <groupId>com.cariochi.recordo</groupId>
   <artifactId>recordo-mockserver</artifactId>
   <version>2.1.1</version>
   <scope>test</scope>
</dependency>
```

# JUnit Setup

Register `RecordoExtension` on every test class that uses Recordo parameter resolution, fixture generation, MockMvc
helpers, or HTTP recording:

```java
import com.cariochi.recordo.core.RecordoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RecordoExtension.class)
class MyTest {
}
```

You can combine Recordo with other JUnit extensions:

```java

@ExtendWith({RecordoExtension.class, MockitoExtension.class})
class ServiceTest {
}
```

# Resource Files

Recordo treats annotation paths as paths under the configured test resource root. By default, that root is:

```properties
resources.root.folder=/src/test/resources
```

For example, `@Read("/books/book.json")` points to:

```text
src/test/resources/books/book.json
```

To override the location, create `recordo.properties` on the test classpath:

```properties
resources.root.folder=/src/integrationTest/resources
```

Generated files are written under the same root.

## Supported File Formats

Recordo supports both **JSON** and **YAML** fixture files. The format is detected automatically by file extension:

| Extension | Format |
|-----------|--------|
| `.json`   | JSON   |
| `.yaml`, `.yml` | YAML |

You can use YAML for any fixture — `@Read`, `assertAsJson`, and `@MockServer` all support it:

```java
@Read("/books/book.yaml")
private Book book;

assertAsJson(actual).isEqualTo("/books/book.yaml");
```

```java
@Test
@MockServer("/mockserver/should_retrieve_gists.rest.yml")
void should_retrieve_gists() { ... }
```

When a fixture file is missing, Recordo generates and writes it in the same format as the requested path extension.

# ObjectMapper Resolution

Recordo uses Jackson 3 for JSON serialization and deserialization. When an API accepts an `objectMapper` name, Recordo
looks for an `ObjectMapper` or `JsonMapper`
with that name.

Object mappers can come from:

- Spring beans, when a Spring test context is available.
- Test class fields annotated with `@RecordoBean`.
- Recordo's default mapper, when no custom mapper is selected.

Example:

```java
import com.cariochi.recordo.core.Recordo;
import com.cariochi.recordo.core.RecordoBean;
import com.cariochi.recordo.read.Read;
import com.cariochi.recordo.read.RecordoObjectFactory;
import tools.jackson.databind.json.JsonMapper;

@RecordoObjectFactory
interface BookFactory {

    @Read(value = "/books/book.json", objectMapper = "testMapper")
    Book book();
}

@ExtendWith(RecordoExtension.class)
class JsonTest {

    @RecordoBean
    private final JsonMapper testMapper = JsonMapper.builder().build();

    private final BookFactory books = Recordo.create(BookFactory.class);

    @Test
    void reads_with_named_mapper() {
        Book book = books.book();
    }
}
```

If more than one mapper is available, prefer passing `objectMapper = "..."` explicitly.

# Configuration

Configuration is resolved in this priority order:

1. **System property** — `-Drecordo.resources.root.folder=...` (useful for CI)
2. **Spring Environment** — `recordo.resources.root.folder` in `application-test.yaml` or `@TestPropertySource`
3. **`recordo.properties`** — file on the test classpath
4. **Built-in defaults**

Create `recordo.properties` in test resources to override defaults:

```properties
resources.root.folder=/src/test/resources
http.mocks.headers.included=Authorization, Content-Encoding, Content-Type, Accept, Accept-Charset, Location, Link, X-Auth
http.mocks.headers.sensitive=Authorization, X-Auth
```

Or with Spring:

```yaml
# application-test.yaml
recordo:
  resources.root.folder: /src/integrationTest/resources
```

Or on the command line:

```bash
mvn test -Drecordo.resources.root.folder=/custom/path
```

Available properties:

| Property                       | Default                                                                                         | Description                                                            |
|--------------------------------|-------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| `resources.root.folder`        | `/src/test/resources`                                                                           | Base folder for reading and writing Recordo files.                     |
| `http.mocks.headers.included`  | `Authorization, Content-Encoding, Content-Type, Accept, Accept-Charset, Location, Link, X-Auth` | Header names saved in HTTP recordings and used during replay matching. |
| `http.mocks.headers.sensitive` | `Authorization, X-Auth`                                                                         | Header names masked as `********` in saved HTTP recordings.            |

Header names are matched case-insensitively.

# Read Module

The Read module loads test data from files and turns fixture files into reusable test data factories. If the target file
is missing, Recordo generates a value
for the requested type, writes it to disk, and returns it to the test.

For new tests, prefer `@RecordoObjectFactory` interfaces. They keep fixture paths, named fixture methods, and
modification rules in one reusable place, so test
code reads like a small domain-specific fixture API.

## Object Factory Interfaces

Define an interface annotated with `@RecordoObjectFactory`. Methods annotated with `@Read` load objects from files.
Methods annotated with `@Modify` create
fluent modifiers that can be chained before a fixture method is called.

```java
import com.cariochi.objecto.Modify;
import com.cariochi.recordo.read.RecordoObjectFactory;
import com.cariochi.recordo.read.Read;

@RecordoObjectFactory
public interface BookFactory {

    @Read("/books/book.json")
    Book book();

    @Read("/books/books.json")
    List<Book> books();

    @Read("/books/book.json")
    Book book(@Modify("id") Long id);

    @Modify("title")
    BookFactory withTitle(String title);

    @Modify("author.name")
    BookFactory withAuthorName(String name);
}
```

Use `Recordo.create(...)` to create the factory:

```java
import com.cariochi.recordo.core.Recordo;
import com.cariochi.recordo.core.RecordoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    private final BookFactory books = Recordo.create(BookFactory.class);

    @Test
    void should_create_custom_book() {
        Book book = books
                .withTitle("The Left Hand of Darkness")
                .withAuthorName("Ursula Le Guin")
                .book(42L);

        assertThat(book.getId()).isEqualTo(42L);
        assertThat(book.getTitle()).isEqualTo("The Left Hand of Darkness");
    }

    @Test
    void should_load_list_fixture() {
        List<Book> allBooks = books.withAuthorName("Ursula Le Guin").books();
        assertThat(allBooks).isNotEmpty();
    }
}
```

Factory methods can return:

- POJOs and DTOs.
- Arrays and collections such as `List<T>`.
- `String`.
- `byte[]`.

## Fixture Variations

`@Modify` accepts object paths. Paths can target nested properties, collection indexes, and wildcards:

```java

@RecordoObjectFactory
public interface UserFactory {

    @Read("/users/user.json")
    User user();

    @Read("/users/user.json")
    User user(@Modify("id") Long id);

    @Modify("profile.email")
    UserFactory withEmail(String email);

    @Modify("roles[*].name")
    UserFactory withRoleName(String roleName);
}
```

Usage:

```java
User admin = users
        .withEmail("admin@example.com")
        .withRoleName("ADMIN")
        .user(1L);
```

Modifiers are immutable from the caller's point of view: chaining returns a modified factory instance and does not edit
the fixture file.

## Default Methods

Factory interfaces may contain default methods. Recordo still applies modifiers to objects returned by those methods:

```java

@RecordoObjectFactory
interface UserFactory {

    default User admin() {
        return new User(1L, "admin");
    }

    @Modify("name")
    UserFactory withName(String name);
}
```

## Spring Bean Usage

In Spring tests, Recordo can register missing `@RecordoObjectFactory` fields as beans when they are autowired:

```java

@SpringBootTest
@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    @Autowired
    private BookFactory books;

    @Test
    void should_use_factory_bean() {
        Book book = books.withTitle("Dune").book();
        assertThat(book.getTitle()).isEqualTo("Dune");
    }
}
```

## Legacy APIs

Recordo also supports direct `@Read` injection on parameters and fields, plus the lower-level `ObjectFactory<T>` API,
for existing tests. New tests should
prefer `@RecordoObjectFactory` interfaces because they keep fixture paths and modifications named, reusable, and easier
to read.

# Assertions Module

The Assertions module compares actual values against files and creates missing expected files on first run.

## JSON and YAML Assertions

Use `JsonAssertion.assertAsJson(actual)` to compare an object against a fixture file. Both JSON and YAML files are supported — the format is detected from the file extension:

```java
import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@Test
void should_match_json() {
    Page<Book> books = bookService.findAll();

    assertAsJson(books)
            .isEqualTo("/books/books_page.json");   // JSON fixture
}

@Test
void should_match_yaml() {
    Book book = bookService.findById(1L);

    assertAsJson(book)
            .isEqualTo("/books/book.yaml");          // YAML fixture
}
```

Options:

```java
assertAsJson(books)
        .

including("content[*].id","content[*].title")
        .

excluding("content[*].createdAt")
        .

allowExtraFields(true)
        .

withStrictOrder(false)
        .

isEqualTo("/books/short_books.json");
```

| Method                      | Description                                                            |
|-----------------------------|------------------------------------------------------------------------|
| `including(String...)`      | Compare only selected JSON paths.                                      |
| `excluding(String...)`      | Ignore selected JSON paths.                                            |
| `allowExtraFields(boolean)` | Allow extra fields in actual JSON when comparing. Defaults to `false`. |
| `withStrictOrder(boolean)`  | Require array order to match. Defaults to `true`.                      |
| `using(ObjectMapper)`       | Use a specific mapper for this assertion.                              |

JSON paths support nested properties, collection indexes, and `*` wildcards:

```java
assertAsJson(order)
        .

including("id","items[*].sku","items[*].price")
        .

isEqualTo("/orders/short_order.json");
```

## AssertJ Condition

Use `JsonCondition.equalAsJsonTo(...)` when you prefer AssertJ condition syntax:

```java
import static com.cariochi.recordo.assertions.JsonCondition.equalAsJsonTo;
import static org.assertj.core.api.Assertions.assertThat;

assertThat(books)
        .

is(equalAsJsonTo("/books/books_page.json")
                .

withStrictOrder(false));
```

## CSV Assertions

Use `CsvAssertion.assertCsv(actualCsv)`:

```java
import static com.cariochi.recordo.assertions.CsvAssertion.assertCsv;

@Test
void should_match_csv() {
    String actualCsv = "id,name\n1,Ada\n2,Grace";

    assertCsv(actualCsv)
            .withHeaders(true)
            .withStrictOrder(false)
            .isEqualsTo("/csv/users.csv");
}
```

Options:

| Method                      | Description                           |
|-----------------------------|---------------------------------------|
| `withHeaders(boolean)`      | Treat the first row as a header row.  |
| `withStrictOrder(boolean)`  | Require row order to match.           |
| `withColumnSeparator(char)` | Use a separator other than comma.     |
| `withLineSeparator(String)` | Use a line separator other than `\n`. |

## First Run and Failures

When an expected JSON or CSV file does not exist, Recordo writes the actual value to that path and fails the assertion.
Review the generated file and rerun the
test.

When comparison fails and an expected file exists, Recordo writes the actual value next to it in an `ACTUAL/` folder:

```text
src/test/resources/books/books.json
src/test/resources/books/ACTUAL/books.json
```

The `ACTUAL/` folder is a debugging aid and should not be committed.
See [Reviewing ACTUAL Files](#reviewing-actual-files) for a practical review workflow.

Recommended `.gitignore`:

```gitignore
**/ACTUAL/
```

# Spring MockMvc Module

The MockMvc module lets tests call Spring MVC controllers through typed API client interfaces. This is the recommended
style for new tests because the client
keeps request mappings, parameters, headers, bodies, and return types in one reusable contract.

A Spring test context must contain exactly one usable `MockMvc` instance.

## Typed API Clients

Define an interface that looks like a Spring MVC controller client:

```java
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.RecordoApiClient;
import com.cariochi.recordo.mockmvc.Response;
import org.springframework.web.multipart.MultipartFile;

@RecordoApiClient(interceptors = {LocaleInterceptor.class, AuthInterceptor.class})
@RequestMapping("/users")
public interface UserApiClient {

    @GetMapping("/{id}")
    UserDto getById(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    Response<UserDto> getByIdAsResponse(@PathVariable int id, @RequestParam("name") String name);

    @GetMapping("/{id}")
    Request<UserDto> getByIdAsRequest(@PathVariable int id, @RequestParam("name") String name);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    UserDto create(@RequestBody UserDto user);

    @PostMapping("/{id}/upload")
    String upload(
            @PathVariable int id,
            @RequestParam MultipartFile file1,
            @RequestParam File file2,
            @RequestParam("prefix") String prefix
    );

    @DeleteMapping("/{id}")
    void delete(@PathVariable int id);

    @GetMapping
    Page<UserDto> findAll(@RequestParam("count") int count, Pageable pageable);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorDto getUnauthorized(@PathVariable int id, @RequestHeader("Authorization") String auth);
}
```

Use it either as a Spring bean or create it manually through Recordo:

```java

@WebMvcTest(UserController.class)
@ExtendWith(RecordoExtension.class)
class UserControllerClientTest {

    @Autowired
    private UserApiClient api;

    private final UserApiClient api2 = Recordo.create(UserApiClient.class);

    @Test
    void should_call_controller() {
        UserDto user = api.getById(1, "Test User");
        assertAsJson(user).isEqualTo("/mockmvc/user.json");
    }

    @Test
    void should_upload_files() {
        MultipartFile file1 = new MockMultipartFile("file1", "Upload File 1".getBytes());
        File file2 = File.builder().name("file2").content("Upload File 2".getBytes()).build();

        String content = api.upload(1, file1, file2, "File content");

        assertThat(content).isEqualTo("File content: Upload File 1");
    }
}
```

Supported Spring annotations include:

| Annotation                                                                                         | Purpose                                                                                                          |
|----------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` | Define the request path, HTTP method, params, consumes, and produces metadata.                                   |
| `@PathVariable`                                                                                    | Bind method arguments into URI template variables.                                                               |
| `@RequestParam`                                                                                    | Add query parameters or multipart fields, including `Collection` values and `Pageable`.                          |
| `@RequestHeader`                                                                                   | Add request headers from method arguments.                                                                       |
| `@RequestBody`                                                                                     | Serialize a method argument as the request body.                                                                 |
| `@ResponseStatus`                                                                                  | Set the expected response status for non-`200 OK` responses and still deserialize the body into the return type. |

Supported return styles:

| Return style     | Example                                                | Behavior                                                                                                         |
|------------------|--------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| Direct body      | `UserDto`, `Page<UserDto>`, `String`, `byte[]`, `void` | Recordo performs the request immediately and returns the deserialized response body.                             |
| Full response    | `Response<UserDto>`                                    | Recordo performs the request immediately and returns status, headers, and deserialized body.                     |
| Deferred request | `Request<UserDto>`                                     | Recordo prepares the request only. The test can add headers, params, files, or body data, then call `perform()`. |

## Request Interceptors

Implement `RequestInterceptor` to mutate outgoing requests:

```java
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.RequestInterceptor;

public class AuthInterceptor implements RequestInterceptor {

    @Override
    public Request<?> apply(Request<?> request) {
        if (!request.headers().containsKey("Authorization")) {
            return request.header("Authorization", "Bearer test-token");
        }
        return request;
    }
}
```

Interceptors can be declared on `@RecordoApiClient`. If a Spring bean of the interceptor type exists, Recordo uses it;
otherwise it creates the interceptor
with a default constructor. Legacy parameter annotations can also declare interceptors.

## Legacy APIs

Recordo also supports parameter-level `@Get`, `@Post`, `@Put`, `@Patch`, `@Delete`, and `@Perform` annotations for
existing tests. New tests should prefer
`@RecordoApiClient` interfaces.

# MockServer Module

The MockServer module records real HTTP requests and responses on the first run, then replays them from fixture files on
later runs.

Supported clients:

- Spring `RestTemplate`.
- Spring `RestClient`.
- OkHttp `OkHttpClient`.
- Apache HttpClient 5 `HttpClient`.
- OpenFeign when Feign uses one of the supported underlying clients.

## Basic Usage

Add `@MockServer` to a test method to enable HTTP recording and replay for that test. Use one annotation for a single
client or endpoint group, or repeat
`@MockServer` when the test needs separate recordings for multiple clients or URL patterns.

```java
import com.cariochi.recordo.mockserver.MockServer;

@SpringBootTest
@ExtendWith(RecordoExtension.class)
class GitHubClientTest {

    @Autowired
    private GitHubClient gitHubClient;

    @Test
    @MockServer("/mockserver/github/get_gists.rest.json")
    void should_retrieve_gists() {
        List<GistResponse> gists = gitHubClient.getGists();
        assertAsJson(gists).isEqualTo("/mockserver/github/gists.json");
    }
}
```

First run:

- The target mock file is missing.
- Real HTTP calls are executed.
- Recordo stores request/response interactions in JSON.

Later runs:

- Recordo reads the mock file.
- HTTP calls are intercepted.
- Saved responses are returned without network access.
- Requests are compared with the saved requests.

To record the HTTP interactions again, delete the existing mock file or folder and rerun the test.

If not all saved interactions are used, the test fails with `Not all mocks requests were called`.

## Storage Modes

Single file mode stores all interactions in one file. Both JSON and YAML are supported:

```java
@MockServer("/mockserver/github/gists.rest.json")   // JSON
@MockServer("/mockserver/github/gists.rest.yml")    // YAML
```

Folder mode stores each interaction as a separate JSON file:

```java
@MockServer("/mockserver/github/gists")
```

Folder mode file names are generated from request order, HTTP method, host, and path:

```text
001__POST__api.github.com__gists.json
002__GET__api.github.com__gists_31e4458e2fbb1e073787790766268622.json
```

## MockServer Annotation

`@MockServer` is repeatable and is applied to test methods.

| Attribute          | Default  | Description                                                          |
|--------------------|----------|----------------------------------------------------------------------|
| `value`            | required | Path to a `.rest.json`, `.rest.yml` file or folder under the resource root. |
| `client`           | `""`     | Name of the HTTP client bean or `@RecordoBean` field to use.         |
| `objectMapper`     | `""`     | Name of the mapper used to read/write recordings.                    |
| `urlPattern`       | `"**"`   | URL matcher. Supports `?`, `*`, and `**`.                            |
| `allowExtraFields` | `false`  | Allow extra JSON fields in request matching.                         |
| `strictOrder`      | `true`   | Require strict array order in request matching.                      |

Example with relaxed request body comparison:

```java

@Test
@MockServer(
        value = "/mockserver/search.rest.json",
        allowExtraFields = true,
        strictOrder = false
)
void should_search() {
}
```

## Client Selection

If `client` is empty, Recordo tries to find a single supported HTTP client. If several candidates exist, name the client
bean or `@RecordoBean` field
explicitly:

```java

@Autowired
private RestTemplate booksRestTemplate;

@Autowired
private RestTemplate authorsRestTemplate;

@Test
@MockServer(client = "booksRestTemplate", value = "/mockserver/books.rest.json")
@MockServer(client = "authorsRestTemplate", value = "/mockserver/authors.rest.json")
void should_call_two_services() {
}
```

When requests should be routed by URL instead of client name, use repeatable `@MockServer` annotations with different
URL patterns:

```java

@Test
@MockServer(urlPattern = "https://books.server/**", value = "/mockserver/books.rest.json")
@MockServer(urlPattern = "https://authors.server/**", value = "/mockserver/authors.rest.json")
void should_route_by_url() {
}
```

URL pattern wildcards:

| Wildcard | Meaning                     |
|----------|-----------------------------|
| `?`      | One character.              |
| `*`      | Zero or more characters.    |
| `**`     | Zero or more path segments. |

## Spring Client Integration

For Spring tests, prefer adding the Recordo interceptor directly to the HTTP client bean used by application code.
Recordo finds that interceptor during
`@MockServer` tests and attaches the active recording/replay handler to it. Outside `@MockServer` tests the interceptor
passes requests through unchanged.

### RestTemplate

```java
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Bean
RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
            .interceptors(new RestTemplateInterceptor())
            .build();
}
```

### RestClient

```java
import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@Bean
RestClient restClient() {
    return RestClient.builder()
            .requestInterceptor(new RestClientInterceptor())
            .build();
}
```

### OkHttp

```java
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

@Bean
OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder()
            .addInterceptor(new OkhttpInterceptor())
            .build();
}
```

### Apache HttpClient 5

```java
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInterceptor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;

@Bean
HttpClient httpClient() {
    return HttpClients.custom()
            .addExecInterceptorFirst("recordo", new ApacheInterceptor())
            .build();
}
```

Recordo interceptors:

- `RestTemplateInterceptor`.
- `RestClientInterceptor`.
- `OkhttpInterceptor`.
- `ApacheInterceptor`.

Recordo can also install an interceptor automatically when there is exactly one supported client bean, or one supported
client marked as `@Primary`. This can
be useful for small tests, but explicit client wiring is recommended for projects with multiple clients or
production-like Spring contexts.

```java

@Bean
RestTemplate restTemplate() {
    return new RestTemplate();
}
```

## Feign Integration

Feign is supported when it uses one of the supported underlying HTTP clients. Add the Recordo interceptor to the
underlying client, then expose that client
through Feign.

With Feign OkHttp:

```java
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInterceptor;
import feign.Client;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

@Bean
OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder()
            .addInterceptor(new OkhttpInterceptor())
            .build();
}

@Bean
Client feignClient(OkHttpClient client) {
    return new feign.okhttp.OkHttpClient(client);
}
```

With Feign Apache HttpClient 5:

```java
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInterceptor;
import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;

@Bean
HttpClient httpClient() {
    return HttpClients.custom()
            .addExecInterceptorFirst("recordo", new ApacheInterceptor())
            .build();
}

@Bean
Client feignClient(HttpClient httpClient) {
    return new ApacheHttp5Client(httpClient);
}
```

## Non-Spring Usage

Annotate a supported client field with `@RecordoBean` and add the Recordo interceptor to that client:

```java

@ExtendWith(RecordoExtension.class)
class GitHubServiceTest {

    @RecordoBean
    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new OkhttpInterceptor())
            .build();

    private final GitHubService service = new GitHubService(client);

    @Test
    @MockServer("/mockserver/github/gists.mock.json")
    void should_replay_http() {
        List<GistDto> gists = service.getGists();
        assertAsJson(gists).isEqualTo("/mockserver/github/gists.json");
    }
}
```

## Recorded Headers

Recordo stores only headers listed in `http.mocks.headers.included`. Sensitive headers listed in
`http.mocks.headers.sensitive` are masked:

```json
{
  "request": {
    "headers": {
      "authorization": "********",
      "content-type": "application/json"
    }
  }
}
```

The first recording run executes real HTTP calls with the credentials configured for the test environment. Use
`http.mocks.headers.sensitive` to keep secrets
out of committed recordings. After recording and debugging, review the mock files and replace any real credentials or
user data in bodies, URLs, or non-masked
headers with placeholders before committing.

Keep real credentials in local-only configuration while recording. Do not commit them in Spring test properties, `.env`
files, local config files, or IDE run
configurations.

## Debugging Failed Replay

When an actual request does not match the next expected recorded request, Recordo writes the actual interaction into an
`ACTUAL/` folder.

Single file mode:

```text
src/test/resources/mockserver/github/gists.rest.json
src/test/resources/mockserver/github/ACTUAL/gists.rest.json
```

Folder mode:

```text
src/test/resources/mockserver/github/gists/ACTUAL/
```

Diff the expected and actual files, then either fix the test/client behavior or update the committed recording
intentionally.

# Reviewing ACTUAL Files

When a test fails on `assertAsJson`, `assertCsv`, or `@MockServer`, use your IDE's file comparison tool to compare the
expected file with the file generated in
the `ACTUAL/` folder. This is usually the fastest way to see whether the test exposed a real regression or the expected
fixture needs to be updated.

If the failure is expected, for example after an intentional change in business logic, and the `ACTUAL` file is correct,
replace the expected file with the
reviewed `ACTUAL` file. You can move it from the `ACTUAL/` folder into the parent folder, or use the IDE compare view to
copy only the intended changes from
`ACTUAL` into the expected file.

# Recommended Project Hygiene

Commit:

- Source tests.
- Reviewed JSON fixtures.
- Reviewed CSV fixtures.
- Reviewed HTTP mock recordings.

Do not commit:

- `ACTUAL/` folders.
- Generated fixtures that have not been reviewed.
- Recordings containing real credentials or user data.

Recommended `.gitignore`:

```gitignore
**/ACTUAL/
```

# Troubleshooting

## A fixture file was generated and the test failed

That is expected on first run for assertions. Review the generated file, commit it if correct, and rerun the test.

## Recordo cannot find my ObjectMapper

If you pass `objectMapper = "customMapper"`, make sure there is a Spring bean or an `@RecordoBean` field with exactly
that name.

```java

@RecordoBean
private final JsonMapper customMapper = JsonMapper.builder().build();
```

## MockServer selected the wrong client

Pass `client` explicitly:

```java
@MockServer(client = "externalApiRestTemplate", value = "/mockserver/external.rest.json")
```

## MockServer fails with `Not all mocks requests were called`

The recording contains more expected interactions than the test executed. Remove unused interactions from the recording
or update the test so it performs the
expected calls.

## Request replay fails after harmless response changes

Recordo matches requests, not responses, during replay. Check the `ACTUAL/` request file. If request body order or
additional fields are expected to vary,
configure `allowExtraFields` or `strictOrder` on `@MockServer`.

## Spring MockMvc parameter injection does not work

Ensure the test has:

```java
@ExtendWith(RecordoExtension.class)
```

and that the Spring test context has a single `MockMvc` instance.

# License

Recordo is distributed under the [Apache License 2.0](LICENSE).
