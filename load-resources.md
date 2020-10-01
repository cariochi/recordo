# Load Resources

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

