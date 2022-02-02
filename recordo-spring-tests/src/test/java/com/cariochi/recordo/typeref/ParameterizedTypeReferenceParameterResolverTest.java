package com.cariochi.recordo.typeref;

import com.cariochi.recordo.books.dto.Book;
import com.cariochi.recordo.core.RecordoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class ParameterizedTypeReferenceParameterResolverTest {

    private static final ParameterizedTypeReference<Page<Book>> TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Test
    void test(ParameterizedTypeReference<Page<Book>> type) {
        assertThat(type).isEqualTo(TYPE_REFERENCE);
    }

}
