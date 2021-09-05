package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.RecordoExtension;
import com.cariochi.recordo.dto.TestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class ParameterizedTypeReferenceParameterResolverTest {

    private static final ParameterizedTypeReference<Page<TestDto>> TYPE_REFERENCE = new ParameterizedTypeReference<>() {};

    @Test
    void test(ParameterizedTypeReference<Page<TestDto>> type) {
        assertThat(type).isEqualTo(TYPE_REFERENCE);
    }

}
