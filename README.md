# What is Recordo

**Recordo** is a JUnit extension for fast, deterministic, and accurate tests. It implements common test functionality in
a declarative way and helps to handle json resources by recording or generating json files if they are absent.

# Documentation

Please, see the recently published documentation [here](https://www.cariochi.com). Although it has been published, it is
still under development and there may be some sections unfinished or missing.

# Quick Start

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
    <version>1.2.1</version>
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
    <version>1.2.1</version>
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
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

### Examples

```java
@Test
@MockServer("/mockServer/get_gists.rest.json")
void should_retrieve_gists() {
    ...
    List<GistResponse> gists = restClient.getGists();
    ...
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
    <version>1.2.1</version>
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
    @Post("/books") Request<Book> request
) {
   BookDto book = ...
   Response<BookDto> response=request.body(book).perform();
}
```

# License

**Recordo** propject is licensed under the MIT License. See
the [LICENSE](https://github.com/cariochi/recordo/blob/master/LICENSE) file for details.

