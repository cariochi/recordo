# Recordo
`Recordo` is a declarative testing JUnit 5 extension for fast, deterministic, and accurate tests.

# Usage
1. Add maven dependency
2. Extend the test class with @ExtendWith(RecordoExtension.class)
```
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.1.5</version>
    <scope>test</scope>
</dependency>
```

## Data preparation
Load test input data from resources. 

Annotations: `@Given`.

- If the file is absent, a new file with a randomly generated object will be created.
#### Usage
```
    @Test
    void should_create_book(
        @Given("/books/new_book.json") Book book
    ) {
        Book created = bookService.create(book);
        // assertions
    }
```

## Assertions 
Assert actual value in a test equals to expected.

Annotations: `@Verify`. 

The expected object is loaded from a resource.  
- If a file is absent, the actual result will be saved as expected.
- If an assertion fails new "actual" object file will be created.
#### Usage
```
    @Test
    void should_get_book_by_id(
            @Verify("/books/book.json") Expected<Book> expected
    ) {
        Book actual = bookService.findById(1L);
        expected.assertEquals(actual);
    }
```
## Mocking HTTP resources
`@MockHttp` annotation is used to automatically record and replay HTTP network interaction.
#### Initialization
- OkHttp
```
    @EnableRecordo
    private OkHttpClient client;
```
- Apache HttpClient
```
    @EnableRecordo
    private HttpClient httpClient;
```
#### Usage
```
    @Test
    @MockHttp("/mockhttp/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHubClient.getGists();
    }
```
## Declarative MockMvc
Use Spring MockMvc in declarative way.

Annotations: `@Get`, `@Post`, `@Put`, `@Patch`, `@Delete`, `@Headers`, `@Body`.
#### Initialization
```
    @EnableRecordo
    private MockMvc mockMvc;
```
#### Usage
- GET Request
```
    @Test
    void should_get_books(
            @Get("/users/{id}/books?sort={sort}") @Headers("locale: UA") Request<List<Book>> request
    ) {
        Response<List<Book>> response = request.execute(1, "name");
        List<Book> books = response.getContent();
        // assertions
    }
```
- GET Response 
```
    @Test
    void should_get_books(
           @Get("/users/1/books?sort=name") @Headers("locale: UA") Response<List<Book>> response
    ) {
        List<Book> books = response.getContent();
        // assertions
    }
```
- GET Response Body 
```
    @Test
    void should_get_books(
           @Get("/users/1/books?sort=name") @Headers("locale: UA") List<Book> books
    ) {
        // assertions
    }
```
- POST Request 
```
    @Test
    void should_save_book(
            @Post("/books") Request<Book> request
    ) {
        Response<Book> response = request.withBody(new Book()).execute();
        Book book = response.getContent();
        // assertions
    }
```
- POST Request 
```
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Request<Book> request
    ) {
        Response<Book> response = request.execute();
        Book book = response.getContent();
        // assertions
    }
```
- POST Response 
```
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Response<Book> response
    ) {
        Book book = response.getContent();
        // assertions
    }
```
- POST Response Body 
```
    @Test
    void should_save_book(
            @Post("/books") @Body("/mockmvc/new_book.json") Book savedBook
    ) {
        // assertions
    }
```
