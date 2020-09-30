# Recordo
`Recordo` is a declarative testing JUnit 5 extension for fast, deterministic, and accurate tests.

# Usage

### Maven dependency
```xml
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.1.8</version>
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

### Enable ObjectMapper to be used by Recordo (Optional)  

```java
import com.cariochi.recordo.EnableRecordo;
```

```java
@EnableRecordo
private ObjectMapper objectMapper;
```

# Test resources loading

Load objects from json files. 

### Imports

```java
import com.cariochi.recordo.Given;
```

### Examples

```java
@Test
void should_create_book(
    @Given("/books/book.json") Book book
) {
    ...
}
```

```java
@Test
void should_create_book(
    @Given("/books/books.json") List<Book> books
) {
    ...
}
```
# Assertions 

Assert that actual value is equal to expected.

- If a file is absent, the actual result will be saved as expected.
- If an assertion fails new "actual" object file will be created.

### Imports

```java
import com.cariochi.recordo.Given;
import com.cariochi.recordo.given.Assertion;
```

### Examples

```java
@Test
void should_get_book_by_id(
        @Given("/books/book.json") Assertion<Book> assertion
) {
    final Book actual = bookService.findById(1L);
    assertion.assertAsExpected(actual);
}
```

```java
@Test
void should_get_books_by_author(
        @Given("/books/author.json") Author author,
        @Given("/books/short_books.json") Assertion<Page<Book>> assertion
) {
    Page<Book> actual = bookService.findAllByAuthor(author);
    assertion
            .included("content.id", "content.title", "content.author.id")
            .extensible(true)
            .assertAsExpected(aclual);
}
```

```java
@Test
void should_get_all_books(
        @Given("/books/all_books.json") Assertion<List<Book>> assertion
) {
    List<Book> actual = bookService.findAll();
    assertion
            .excluded("description", "author.comments")
            .strictOrder(false)
            .assertAsExpected(actual);
}
```

# Mocking HTTP resources

Record and replay HTTP network interaction for a test.

### Imports

```java
import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.MockHttpServer;
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
@MockHttpServer("/mockhttp/should_retrieve_gists.rest.json")
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
import com.cariochi.recordo.mockhttp.client.MockHttpRequest;
import com.cariochi.recordo.mockhttp.client.MockHttpGet;
import com.cariochi.recordo.mockhttp.client.MockHttpPost;
import com.cariochi.recordo.mockhttp.client.MockHttpPut;
import com.cariochi.recordo.mockhttp.client.MockHttpPatch;
import com.cariochi.recordo.mockhttp.client.MockHttpDelete;

import com.cariochi.recordo.mockhttp.client.MockHttpClient;
import com.cariochi.recordo.mockhttp.client.Request;
import com.cariochi.recordo.mockhttp.client.Response;
import com.cariochi.recordo.mockhttp.client.RequestInterceptor;
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
        @MockHttpGet("/users/1/books") Page<Book> books
) {
   ...
}
```

```java
@Test
void should_get_books(
        @MockHttpGet(value = "/users/{id}/books?sort={sort}", headers="locale=UA") Request<Page<Book>> request
) {
    ...
    Response<Page<Book>> response = request.parameters(1, "name").execute();
    Page<Book> books = response.getContent();
    ...
}
```

```java
@Test
void should_save_book(
        @MockHttpPost("/books") Request<Book> request
) {
    ...
    Response<Book> response = request.body(new Book()).execute();
    Book book = response.getContent();
    // assertions
}
```

```java
@Test
void should_save_book(
        @MockHttpPost(value = "/books", body = "/mockmvc/new_book.json") Request<Book> createBookRequest
) {
    ...
    Book createdBook = createBookRequest.execute().getContent();
    ...
}
```

```java
@Test
void should_save_book(
        @MockHttpPost(value = "/books", body = "/mockmvc/new_book.json") Book createdBook
) {
    ...
}
```

```java
@Test
void should_update_book(
        @MockHttpPost(value = "/books", body = "/mockmvc/new_book.json") Book updatedBook
) {
     ...
}
```

```java
@Test
void should_patch_book(
        @MockHttpPatch(value = "/books/1", body = "/mockmvc/book.json") Book patchedBook
) {
    ...
}
```

```java
@Test
void should_delete_book(
        @MockHttpDelete("/books/1") Request<Void> request
) {
    ...
    Response<Void> response = request.execute();
    ...
}
```
