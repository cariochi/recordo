# Recordo
`Recordo` is a declarative testing JUnit 5 extension for fast, deterministic, and accurate tests.

# Usage
1. Add maven dependency
2. Extend the test class with @ExtendWith(RecordoExtension.class)

```xml
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.1.5</version>
    <scope>test</scope>
</dependency>
```

# Data preparation

Load objects from json files. 

Annotations: `@Given`.

- If the file is absent, a new random data file will be created.

#### Example

```java
    @Test
    void should_create_book(
        @Given("/books/new_book.json") Book book
    ) {
        Book created = bookService.create(book);
        // assertions
    }
```

# Assertions 

Assert that actual value equals to expected.

Annotations: `@Verify`. 

- If a file is absent, the actual result will be saved as expected.
- If an assertion fails new "actual" object file will be created.

#### Example

```java
    @Test
    void should_get_book_by_id(
            @Verify("/books/book.json") Expected<Book> expected
    ) {
        Book actual = bookService.findById(1L);
        expected.assertEquals(actual);
    }
```

# Mocking HTTP resources

Record and replay HTTP network interaction for a test.

Annotations: `@MockHttp`.

#### Initialization

- OkHttp

```java
    @EnableRecordo
    private OkHttpClient client;
```

- Apache HttpClient

```java
    @EnableRecordo
    private HttpClient httpClient;
```

#### Example

```java
    @Test
    @MockHttp("/mockhttp/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHubClient.getGists();
    }
```

# Declarative MockMvc

Use Spring MockMvc in declarative way.

Annotations: `@Get`, `@Post`, `@Put`, `@Patch`, `@Delete`, `@Headers`, `@Body`.

#### Initialization
```java
    @EnableRecordo
    private MockMvc mockMvc;
```

#### Examples

```java
    @Test
    void should_get_books(
            @Get("/users/{id}/books?sort={sort}") @Headers("locale: UA") Request<Page<Book>> request
    ) {
        Response<Page<Book>> response = request.execute(1, "name");
        Page<Book> books = response.getContent();
        // assertions
    }
```

```java
    @Test
    void should_get_books(
           @Get("/users/1/books?sort=name") @Headers("locale: UA") Response<Page<Book>> response
    ) {
        Page<Book> books = response.getContent();
        // assertions
    }
```

```java
    @Test
    void should_get_books(
           @Get("/users/1/books?sort=name") @Headers("locale: UA") Page<Book> books
    ) {
        // assertions
    }
```

```java
    @Test
    void should_save_book(
            @Post("/books") Request<Book> request
    ) {
        Response<Book> response = request.withBody(new Book()).execute();
        Book book = response.getContent();
        // assertions
    }
```

```java
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Request<Book> request
    ) {
        Response<Book> response = request.execute();
        Book book = response.getContent();
        // assertions
    }
```

```java
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Response<Book> response
    ) {
        Book book = response.getContent();
        // assertions
    }
```

```java
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Book savedBook
    ) {
        // assertions
    }
```
