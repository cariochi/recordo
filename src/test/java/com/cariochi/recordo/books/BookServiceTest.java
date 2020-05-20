package com.cariochi.recordo.books;

import com.cariochi.recordo.AuthorService;
import com.cariochi.recordo.Given;
import com.cariochi.recordo.Verify;
import com.cariochi.recordo.books.app.Author;
import com.cariochi.recordo.books.app.Book;
import com.cariochi.recordo.books.app.BookService;
import com.cariochi.recordo.junit5.RecordoExtension;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.when;

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
