package com.cariochi.recordo.books;

import com.cariochi.recordo.books.dto.Author;
import com.cariochi.recordo.books.dto.Book;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.read.Read;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static com.cariochi.recordo.assertions.JsonCondition.equalAsJsonTo;
import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private BookService bookService;

    @SneakyThrows
    @Test
    void test_properties() {
        final Enumeration<URL> resources = currentThread().getContextClassLoader().getResources("");
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            final File dir = new File(resource.getFile());
            final String[] files = dir.list();
            log.info("!!!Folder: {}", dir);
            log.info("!!!Files: {}", files);
        }
    }

    @Test
    void should_get_book_by_id() {
        final Book actual = bookService.findById(1L);
        assertAsJson(actual).isEqualTo("/books/book.json");
        assertThat(actual).is(equalAsJsonTo("/books/book.json"));
    }

    @Test
    void should_get_books_by_author(
            @Read("/books/author.json") Author author
    ) {
        final Page<Book> books = bookService.findAllByAuthor(author);

        assertAsJson(books)
                .including("content.id", "content.title", "content.author.id")
                .isEqualTo("/books/short_books.json");

        assertThat(books)
                .is(equalAsJsonTo("/books/short_books.json")
                        .including("content.id", "content.title", "content.author.id"));

        assertThat(books).haveExactly(1, equalAsJsonTo("/books/book.json"));
    }

    @Test
    void should_create_book(
            @Read("/books/new_book.json") Book book,
            @Read("/books/author.json") Author author
    ) {
        when(authorService.findById(book.getAuthor().getId())).thenReturn(author);
        final Book created = bookService.create(book);

        assertAsJson(created)
                .excluding("id")
                .isEqualTo("/books/created_book.json");

        assertThat(created)
                .is(equalAsJsonTo("/books/created_book.json").excluding("id"))
                .extracting(Book::getId).isNotNull();
    }

    @Test
    void should_add_book_to_shelf(
            @Read("/books/book.json") Book book,
            @Read("/books/books.json") List<Book> books
    ) {
        final Page<Book> merged = bookService.merge(books, book);

        assertAsJson(merged).isEqualTo("/books/expected_books.json");

        assertThat(merged).haveExactly(1, equalAsJsonTo("/books/book.json"));
    }

}
