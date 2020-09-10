# Recordo
`Recordo` is a declarative testing JUnit 5 extension for fast, deterministic, and accurate tests.

# Usage

### Add maven dependency
```xml
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.1.7</version>
    <scope>test</scope>
</dependency>
```
### Imports

```java
import com.cariochi.recordo.RecordoExtension;
```

### Initialization

```java
@ExtendWith(RecordoExtension.class)
class BookServiceTest {
    ...
}
```

### Enable Json Converter to be used in Recordo (Optional)  

```java
import com.cariochi.recordo.EnableRecordo;
```

- Jackson Mapper

```java
@EnableRecordo
private ObjectMapper objectMapper;
```

- Gson

```java
@EnableRecordo
private Gson gson;
```

# Data preparation

Load objects from json files. 

- If the file is absent, a new random data file will be created.

### Imports

```java
import com.cariochi.recordo.Given;
```

### Example

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

- If a file is absent, the actual result will be saved as expected.
- If an assertion fails new "actual" object file will be created.

### Imports

```java


```

### Examples

```java
@Test
void should_get_book_by_id(
        @Verify("/books/book.json") Expected<Book> expected
) {
    Book actual = bookService.findById(1L);
    expected.assertEquals(actual);
}

@Test
void should_get_book_by_id(
        @Verify(value ="/books/book.json", extensible = true) Expected<Book> expected
) {
    Book actual = bookService.findById(1L);
    expected.assertEquals(actual);
}

@Test
void should_get_books(
        @Verify(
                value = "/books/book.json",
                included = {"id", "title", "author.id", "author.name"},
                strictOrder = false
        ) 
        Expected<List<Book>> expected
) {
    List<Book> actual = bookService.findAll();
    expected.assertEquals(actual);
}

@Test
void should_get_books(
        @Verify(
                value = "/books/book.json",
                excluded = {"description", "author.comments"},
                strictOrder = false
        ) 
        Expected<List<Book>> expected
) {
    List<Book> actual = bookService.findAll();
    expected.assertEquals(actual);
}
```

# Mocking HTTP resources

Record and replay HTTP network interaction for a test.

### Imports

```java
import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.MockHttp;
```

### Initialization

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

### Example

```java
@Test
@MockHttp("/mockhttp/should_retrieve_gists.rest.json")
void should_retrieve_gists() {
    ...
    final List<GistResponse> gists = gitHubClient.getGists();
    ...
}
```

# Declarative MockMvc

Use Spring MockMvc in declarative way.

### Imports

```java
import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.mockmvc.GET;
import com.cariochi.recordo.mockmvc.POST;
import com.cariochi.recordo.mockmvc.PUT;
import com.cariochi.recordo.mockmvc.PATCH;
import com.cariochi.recordo.mockmvc.DELETE;
import com.cariochi.recordo.mockmvc.Headers;
import com.cariochi.recordo.mockmvc.Body;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Response;
```

### Initialization

```java
@EnableRecordo
private MockMvc mockMvc;
```

### Examples

```java

@Test
void should_get_books(
        @GET("/users/{id}/books?sort={sort}") @Headers("locale: UA") Request<Page<Book>> request
) {
    ...
    Response<Page<Book>> response = request.execute(1, "name");
    Page<Book> books = response.getContent();
    // assertions
}

@Test
void should_get_books(
        @GET("/users/1/books?sort=name") @Headers("locale: UA") Response<Page<Book>> response
) {
    Page<Book> books = response.getContent();
    // assertions
}

@Test
void should_get_books(
        @GET("/users/1/books?sort=name") @Headers("locale: UA") Page<Book> books
) {
    // assertions
}

@Test
void should_save_book(
        @POST("/books") Request<Book> request
) {
    ...
    Response<Book> response = request.withBody(new Book()).execute();
    Book book = response.getContent();
    // assertions
}

@Test
void should_save_book(
        @POST("/books") @Body("/mockmvc/new_book.json") Request<Book> request
) {
    Response<Book> response = request.execute();
    Book book = response.getContent();
    // assertions
}

@Test
void should_save_book(
        @POST("/books") @Body("/mockmvc/new_book.json") Response<Book> response
) {
    Book book = response.getContent();
    // assertions
}

@Test
void should_save_book(
        @POST("/books") @Body("/mockmvc/new_book.json") Book book
) {
    // assertions
}

@Test
void should_update_book(
        @PUT("/books") @Body("/mockmvc/changed_book.json") Book book
) {
     // assertions
}

@Test
void should_patch_book(
        @PATCH("/books/1") @Body("/mockmvc/book.json") Book book
) {
    // assertions
}

@Test
void should_delete_book(
        @DELETE("/users/1") Request<Void> request
) {
    ...
    Response<Void> response = request.execute();
    // assertions
}

@Test
void should_delete_book(
        @DELETE("/users/1") Response<Void> response
) {
    // assertions
}

```
