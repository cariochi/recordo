# What is Recordo

**Recordo** is a JUnit 5 extension for fast, deterministic, and accurate tests. It implements common test functionality in a declarative way and helps to handle json resources by recording or generating json files if they are absent.

# Documentation

Please, see the recently published documentation [here](https://www.cariochi.com). Although it has been published, it is still under development and there may be some sections unfinished or missing.

# Quick Start

### Import maven dependency
```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo</artifactId>
    <version>1.2.0</version>
    <scope>test</scope>
</dependency>
```

### Extend a test with Recordo extension

```java
@ExtendWith(RecordoExtension.class)
class BookServiceTest {
    ...
}
```

# Features 

### Load Resources 

```java
@Test
void should_create_book(
    @Read("/books/book.json") Book book
) {
    ...
}
```

### Make Assertions

```java
@Test
void should_get_book_by_id() {
    Book book = ...
    JsonAssertion.assertAsJson(book)
        .extensible(true)
        .isEqualTo("/books/book.json");
}
```

```java
@Test
void should_get_books_as_csv() {
    String csv = ...
    CsvAssertion.assertCsv(csv)
        .withHeaders(true)
        .withStrictOrder(false)
        .isEqualTo("/books.csv");
}
```

### Record and Playback  REST Requests

```java
@Test
@MockServer("/mockServer/get_gists.rest.json")
void should_retrieve_gists() {
    ...
    List<GistResponse> gists = restClient.getGists();
    ...
}
```

### Test a Web Layer  

```java
@Test
void should_get_books(
        @Get("/books") Page<Book> books
) {
   ...
}
```

```java
@Test
void should_create_book(
        @Post("/books") Request<Book> request
) {
    BookDto book = ...
    Response<BookDto> response = request.body(book).perform();
}
```

# License

**Recordo** propject is licensed under the MIT License. See the [LICENSE](https://github.com/cariochi/recordo/blob/master/LICENSE) file for details.

