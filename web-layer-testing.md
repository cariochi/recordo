# Web Layer Testing

Use Spring MockMvc in a declarative way.

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

