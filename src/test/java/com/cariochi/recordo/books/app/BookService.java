package com.cariochi.recordo.books.app;

import com.cariochi.recordo.AuthorService;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomUtils.nextLong;

public class BookService {

    private AuthorService authorService = new AuthorService();

    public Book findById(Long id) {
        return books().stream()
                .filter(book -> id.equals(book.getId()))
                .findAny()
                .orElse(null);
    }

    public List<Book> findAllByAuthor(Author author) {
        return books();
    }

    public List<Book> books() {
        return asList(
                book(1L, "Othello"),
                book(2L, "Macbeth"),
                book(3L, "Richard II")
        );
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

    public List<Book> merge(List<Book> books, Book book) {
        return Stream.concat(books.stream(), Stream.of(book))
                .sorted(comparing(Book::getTitle))
                .collect(toList());
    }

    public Book create(Book book) {
        return book
                .withAuthor(authorService.findById(book.getAuthor().getId()))
                .withId(nextLong());
    }
}
