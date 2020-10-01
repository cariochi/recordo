# What is Recordo

**Recordo** is a JUnit 5 extension for fast, deterministic, and accurate tests. It implements common test functionality in a declarative way and helps to handle json resources by recording or generating json files if they are absent.

# Documentation

Please, see our recently published documentation [here](https://www.cariochi.com). Although it has been published, it is still under development and there may be some sections unfinished or missing.

# Features 

### Load Resources 

```java
@Test
void should_create_book(
    @Given("/books/book.json") Book book
) {
    ...
}
```

### Make Assertions

```java
@Test
void should_get_book_by_id(
    @Given("/books/book.json") Assertion<Book> assertion
) {
    final Book actual = bookService.findById(1L);
    assertion.assertAsExpected(actual);
}
```

### Record and Playback  REST Requests

```java
@Test
@MockHttpServer("/mockServer/should_retrieve_gists.rest.json")
void should_retrieve_gists() {
    ...
    final List<GistResponse> gists = restClient.getGists();
    ...
}
```

### Test a Web Layer  

```java
@Test
void should_get_books(
    @MockHttpGet("/books") Page<Book> books
) {
   ...
}
```

# License

**Recordo** propject is licensed under the MIT License. See the [LICENSE](https://github.com/cariochi/recordo/blob/master/LICENSE) file for details.

