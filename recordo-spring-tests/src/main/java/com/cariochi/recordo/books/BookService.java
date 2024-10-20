package com.cariochi.recordo.books;

import com.cariochi.recordo.books.dto.Author;
import com.cariochi.recordo.books.dto.Book;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.RandomUtils.nextLong;

public class BookService {

    private AuthorService authorService = new AuthorService();

    public Book findById(Long id) {
        return books().stream()
                .filter(book -> id.equals(book.getId()))
                .findAny()
                .orElse(null);
    }

    public Page<Book> findAllByAuthor(Author author) {
        return books();
    }

    public Page<Book> books() {
        return new PageImpl<>(List.of(
                book(1L, "Othello"),
                book(2L, "Macbeth"),
                book(3L, "Richard II")
        ));
    }

    public Page<Book> merge(List<Book> books, Book book) {
        return new PageImpl<>(Stream.concat(books.stream(), Stream.of(book))
                .sorted(comparing(Book::getTitle))
                .toList());
    }

    public Book create(Book book) {
        return book
                .withAuthor(authorService.findById(book.getAuthor().getId()))
                .withId(nextLong());
    }

    private Book book(Long id, String title) {
        return Book.builder()
                .id(id)
                .title(title)
                .author(author())
                .build();
    }

    private Author author() {
        return Author.builder()
                .id(1L)
                .firstName("William")
                .lastName("Shakespeare")
                .build();
    }
}
