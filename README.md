# What is Recordo

**Recordo** is a JUnit extension for fast, deterministic, and accurate tests. It implements common test functionality in
a declarative way and helps to handle json resources by recording or generating json files if they are absent.

# Documentation

Please, see the recently published documentation here: [https://cariochi.com/recordo](https://www.cariochi.com/recordo). Although it has been published, it is
still under development and there may be some sections unfinished or missing.

# Quick Start

### Maven dependency

Recordo modules can be added to a project all together or one-by-one separately.

```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-all</artifactId>
    <version>1.2.7</version>
    <type>pom</type>
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

# Modules

## Recordo Read

Loads test resources from JSON files.
An empty json file according to object structure will be automatically created on the first run.
You just need to set expected values, and the test is ready.

### Maven dependency

```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-read</artifactId>
    <version>1.2.7</version>
    <scope>test</scope>
</dependency>
```

### Examples

```java
@Test
void should_create_book(
    @Read("/books/book.json") Book book
) {
        ...
}
```

## Recordo Assertions

1. Asserts that the actual object is equal as JSON to the expected one stored in a file.
2. Asserts that the actual CSV string is equal to the expected one from a file.

JSON and CSV files with actual values will be created on the first run or if they are absent.
You just need to verify them, and the test is ready.

### Maven dependency

```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-assertions</artifactId>
    <version>1.2.7</version>
    <scope>test</scope>
</dependency>
```

### Examples

```java
@Test
void should_get_book_by_id() {
    Book book = ...
        
    assertAsJson(book)
        .extensible(true)
        .isEqualTo("/books/book.json");
}
```

```java
@Test
void should_get_books_as_csv() {
    String csv = ...
        
    assertCsv(csv)
        .withHeaders(true)
        .withStrictOrder(false)
        .isEqualTo("/books.csv");
}
```

## Recordo Mock Server

Records all real REST interactions during the first test run into a file.
Then this file is automatically used for mocking.

### Maven dependency

```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-mockserver</artifactId>
    <version>1.2.7</version>
    <scope>test</scope>
</dependency>
```

### Examples

#### Single MockServer
```java
@Test
@MockServer("/mockServer/get_gists.rest.json")
void should_retrieve_gists() {
    ...
    List<GistResponse> gists = restClient.getGists();
    ...
}
```

#### Multiple MockServers
```java
@Test
@MockServer(urlPattern = "https://books.server/**", value = "/mockserver/multiservers/books-server.rest.json")
@MockServer(urlPattern = "https://authors.server/**", value = "/mockserver/multiservers/authors-server.rest.json")
void should_retrieve_books() {
    List<Book> allBooks = restClient.get("https://books.server/books", listOf(Book.class));
    List<Author> allAuthors = restClient.get("https://authors.server/authors", listOf(Author.class));
    Book book = restClient.get("https://books.server/books/129649986932158", typeOf(Book.class));
    Author author = restClient.get("https://authors.server/authors/1", typeOf(Author.class));
}
```

## Recordo Spring MockMvc

Provides an easy and convenient way to test your String controllers. 
You just need to declare an HTTP request (method, url, headers, etc) and a response type as test method parameters.

### Maven dependency

```markup
<dependency>
    <groupId>com.cariochi.recordo</groupId>
    <artifactId>recordo-spring-mockmvc</artifactId>
    <version>1.2.7</version>
    <scope>test</scope>
</dependency>
```

### Examples

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
    @Post(value = "/books", body = @Content(file = "/books/new_book.json")) Request<BookDto> request
) {
   Response<BookDto> response = request.perform();
}
```

# License

**Recordo** extensions are licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) License. 
