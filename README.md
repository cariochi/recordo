# Recordo
The `Recordo` JUnit extension provides `@Verify` and `@Given` annotations which allows making test fields initialization and a test result verification in a declarative style.

The `@Verify` annotation allows asserting that the expected value (saved in a `JSON` file) is equal to an actual one. The expected value recorded automatically at the first test run or if the file is absent. A developer can specify fields that should be included or excluded, comparison rules (`strictOrder` for arrays and `extensible` for objects), and a file name.

The `@Given` annotation allows initializing a test field from a `JSON` file.

The `@Verify` annotation using concept is:
1. A developer creates a test and runs it the first time;
1. The test fails because the expected file is absent and `Recordo` extension saves the current actual test result as expected;
1. A developer verifies the saved file;
1. The test is ready.
1. In case if something changes in code and the actual value is changed, the test will fail and `Recordo` extension will overwrite the expected file by the new actual value. A developer can compare what was changed in IDE (e.g. compare with the committed version or local history) and reverse file if it was a bug or leave a new version if it was valid logic change.

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
    @Verify(value = "books", included = {"id", "title", "author.id", "author.firstName", "author.lastName"})
    void should_get_books_by_author() {
        books = bookService.findBooks(author);
    }

}
```

