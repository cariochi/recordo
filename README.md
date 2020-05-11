# Recordo

```java
@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    private Author author;
    private Book book;
    private List<Book> books;

    @Test
    @Verify("book")
    void should_get_book_by_id() {
        books = bookService.findBook(33L);
    }

    @Test
    @Given("author")
    @Verify(
        value = "books",
        included = {"id", "title", "author.id", "author.firstName", "author.lastName"}
    )

    void should_get_books_by_author() {
        books = bookService.findBooks(author);
    }

}
```

