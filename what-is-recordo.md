# What is Recordo

{% hint style="info" %}
This documentation is still under development, so you can find some missing sections.
{% endhint %}

**Recordo** is a JUnit 5 extension.

#### Load Resources 

```java
@Test
void should_create_book(
    @Given("/books/book.json") Book book
) {
    ...
}
```

#### Make Assertions

```java
@Test
void should_get_book_by_id(
        @Given("/books/book.json") Assertion<Book> assertion
) {
    final Book actual = bookService.findById(1L);
    assertion.assertAsExpected(actual);
}
```

#### Record and playback all third-party REST requests in a test

```java
@Test
@MockHttpServer("/mockServer/should_retrieve_gists.rest.json")
void should_retrieve_gists() {
    ...
    final List<GistResponse> gists = restClient.getGists();
    ...
}
```

#### Test a web layer  

```java
@Test
void should_get_books(
        @MockHttpGet("/books") Page<Book> books
) {
   ...
}
```

