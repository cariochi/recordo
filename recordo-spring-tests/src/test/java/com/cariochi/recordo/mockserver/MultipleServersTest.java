package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.books.dto.Author;
import com.cariochi.recordo.books.dto.Book;
import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static com.cariochi.recordo.config.Profiles.REST_TEMPLATE;
import static com.cariochi.recordo.config.Profiles.SIMPLE;
import static com.cariochi.recordo.mockmvc.utils.Types.listOf;
import static com.cariochi.recordo.mockmvc.utils.Types.typeOf;

@SpringBootTest
@ActiveProfiles({REST_TEMPLATE, SIMPLE})
@ExtendWith(RecordoExtension.class)
public class MultipleServersTest {

    @Autowired
    @EnableRecordo
    private RestTemplate restTemplate;

    @Test
    @MockServer(urlPattern = "https://books.server/**", value = "/mockserver/multiservers/books-server.rest.json")
    @MockServer(urlPattern = "https://authors.server/**", value = "/mockserver/multiservers/authors-server.rest.json")
    void should_retrieve_gists() {
        final List<Book> allBooks = get("https://books.server/books", listOf(Book.class));
        final List<Author> allAuthors = get("https://authors.server/authors", listOf(Author.class));
        final Book book = get("https://books.server/books/129649986932158", typeOf(Book.class));
        final Author author = get("https://authors.server/authors/1", typeOf(Author.class));
    }

    @Nullable
    private <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(RequestEntity.get(URI.create(url)).build(), responseType).getBody();
    }

}
