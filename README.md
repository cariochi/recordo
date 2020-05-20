# Recordo
`Recordo` is a pretty original and innovative JUnit 4/5 extension.

It provides `@Verify` and `@Given` annotations, which allows making test field initialization and a test result verification in a declarative style.

### `@Given` Annotation
The `@Given` annotation allows initializing a test field from a `json` file.
If a file is absent - `Recordo` will generate a new file with random data for the test field according to its type.


###`@Verify` Annotation
The `@Verify` annotation allows asserting the expected value is equal to an actual one. 
`Recordo` records an expected value automatically at the first test run or if a file is absent. 
A developer can specify fields that should be included or excluded, comparison rules (`strictOrder` for arrays and `extensible` for objects), and a file name.

---

## Getting started

* ### Add maven dependency
```xml
<dependency>
    <groupId>com.cariochi</groupId>
    <artifactId>recordo</artifactId>
    <version>1.0.4</version>
    <scope>test</scope>
</dependency>
```
* ### Add `Recordo` extension/rule to your test
##### JUnit 4:
```java
public class BookServiceTest {

	@Rule
	public RecordoRule recordoRule = new RecordoRule();
	
	...
} 
```
##### JUnit 5:
```java
@ExtendWith(RecordoExtension.class)
class BookServiceTest {
    ...
} 
```
* ### Define test fields
```java
    private Author author;
    private Book book;
```
* ### Annotate your test method with `@Given` or `@Verify` annotation
```java
    @Test
    @Given("author")
    @Verify("books")
    void should_get_books_by_author() {
        books = bookService.findAllByAuthor(author);
    }

```
* ###Test Sample:
```java
@FieldNameConstants
@ExtendWith(MockitoExtension.class)
@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private BookService bookService;

    private Author author;
    private Book book;
    private List<Book> books;

    @Test
    @Verify(Fields.book)
    void should_get_book_by_id() {
        book = bookService.findById(1L);
    }

    @Test
    @Given(Fields.author)
    @Verify(value = Fields.books, included = {"id", "title", "author.id"})
    void should_get_books_by_author() {
        books = bookService.findAllByAuthor(author);
    }

    @Test
    @Given(Fields.book)
    @Given(Fields.author)
    @Verify(value = Fields.book, excluded = "id")
    void should_create_book() {
        when(authorService.findById(book.getAuthor().getId())).thenReturn(author);
        book = bookService.create(book);
    }

    @Test
    @Given(Fields.book)
    @Given(Fields.books)
    @Verify(Fields.books)
    void should_add_book_to_shelf() {
        books = bookService.merge(books, book);
    }
}
```
---
## How it works

#### `@Given` annotation:
1. Create a test and annotate it with `@Given`.
1. Run it the first time.
1. The test fails because the file is absent.
1. `Recordo` generates a new `json` file with random data according to the test field type.
1. You can modify the file if needed.
1. The test is ready.

#### `@Verify` annotation:
1. Create a test and annotate it with `@Verify`.
1. Run it the first time.
1. The test fails because the expected file is absent.
1. `Recordo` saves the current actual test result as expected.
1. You verify the saved file.
1. The test is ready.
1. If you change code, and the actual value is changed - the test will fail with an assertion error. 
`Recordo` will overwrite the expected file with the new actual value. 
You can compare what was changed in IDE (e.g. compare with a committed version or local history) 
and can reverse the file if it was a bug or leave a new version if it was a valid logic change.

---

## Configuration
You can overwrite the default `Recordo` configuration.
For doing that you need to create the `recordo.properties` and define properties that you want to overwrite.

|Property|Description|
|---|---|
|`resources.folder`|Path to the `resorces` folder|
|`given.filename.pattern`|File name pattern for `@Given` annotation|
|`verify.filename.pattern`|File name pattern for `@Verify` annotation|

#### File name pattern placeholders:
* `{TEST_CLASS_FILL_NAME}`
* `{TEST_CLASS_SIMPLE_NAME}`
* `{TEST_METHOD_NAME}`
* `{TEST_FIELD_NAME}`

#### Default properties:
```properties
resources.folder = src/test/resources
given.filename.pattern = {TEST_CLASS_FILL_NAME}/{TEST_METHOD_NAME}/given-{TEST_FIELD_NAME}.json
verify.filename.pattern = {TEST_CLASS_FILL_NAME}/{TEST_METHOD_NAME}/verify-{TEST_FIELD_NAME}.json

```
