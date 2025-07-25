package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.books.dto.Author;
import com.cariochi.recordo.books.dto.Book;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.installers.configs.FeignOkConfig;
import com.cariochi.recordo.mockserver.installers.configs.MultipleRestTemplatesConfig;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.reflecto.types.Types.listOf;
import static org.springframework.core.ParameterizedTypeReference.forType;

@SpringBootTest(classes = {MultipleRestTemplatesConfig.class, FeignOkConfig.class})
@ExtendWith(RecordoExtension.class)
class MultipleServersTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @MockServer(urlPattern = "https://books.server/**", value = "/mockserver/multiservers/books-server.rest.json")
    @MockServer(urlPattern = "https://authors.server/**", value = "/mockserver/multiservers/authors-server.rest.json")
    void should_retrieve_gists() {
        final List<Book> allBooks = get("https://books.server/books", forType(listOf(Book.class)));
        final List<Author> allAuthors = get("https://authors.server/authors", forType(listOf(Author.class)));
        final Book book = get("https://books.server/books/129649986932158", forType(Book.class));
        final Author author = get("https://authors.server/authors/1", forType(Author.class));
    }

    private <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return restTemplate.exchange(RequestEntity.get(URI.create(url)).build(), responseType).getBody();
    }

}
