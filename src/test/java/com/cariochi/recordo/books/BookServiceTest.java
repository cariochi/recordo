package com.cariochi.recordo.books;

import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.books.app.Author;
import com.cariochi.recordo.books.app.AuthorService;
import com.cariochi.recordo.books.app.Book;
import com.cariochi.recordo.books.app.BookService;
import com.cariochi.recordo.junit5.RecordoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(RecordoExtension.class)
class BookServiceTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private BookService bookService;

    @Test
    void should_get_book_by_id(
            @Verify(value = "book") Consumer<Book> verifier
    ) {
        verifier.accept(bookService.findById(1L));
    }

    @Test
    void should_get_books_by_author(
            @Given("author") Author author,
            @Verify(value = "books", included = {"id", "title", "author.id"}) Consumer<List<Book>> verifier
    ) {
        verifier.accept(bookService.findAllByAuthor(author));
    }

    @Test
    void should_create_book(
            @Given("book") Book book,
            @Given("author") Author author,
            @Verify(value = "book", excluded = "id") Consumer<Book> verifier
    ) {
        when(authorService.findById(book.getAuthor().getId())).thenReturn(author);
        verifier.accept(bookService.create(book));
    }

    @Test
    void should_add_book_to_shelf(
            @Given("book") Book book,
            @Given("books") List<Book> books,
            @Verify(value = "books") Consumer<List<Book>> verifier
    ) {
        verifier.accept(bookService.merge(books, book));
    }
}
