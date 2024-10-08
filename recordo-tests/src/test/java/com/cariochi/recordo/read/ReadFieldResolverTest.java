package com.cariochi.recordo.read;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.main.dto.TestDto;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class ReadFieldResolverTest {

    @Read("/read/dto.json")
    private ObjectFactory<TestDto> factory;

    @Test
    void should_create_object_from_factory() {
        TestDto dto = factory.with("children[1].strings[2]", "FAKE").create();
        assertThat(dto.getChildren().get(1).getStrings()[2]).isEqualTo("FAKE");
    }

    @Test
    void should_create_objects_from_factory() {
        final List<TestDto> dtos = IntStream.rangeClosed(100, 102)
                .mapToObj(i -> factory.with("id", i).create())
                .toList();
        assertThat(dtos)
                .extracting(TestDto::getId)
                .containsExactly(100, 101, 102);
    }

}
