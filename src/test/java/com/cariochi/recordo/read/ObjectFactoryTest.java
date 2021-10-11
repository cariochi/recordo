package com.cariochi.recordo.read;

import com.cariochi.recordo.Read;
import com.cariochi.recordo.RecordoExtension;
import com.cariochi.recordo.dto.TestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class ObjectFactoryTest {

    @Read("/read/dto.json")
    private ObjectFactory<TestDto> factory;

    @Test
    void should_create_object_from_factory() {
        TestDto dto = factory.with("children[1].strings[2]", "FAKE").create();
        assertThat(dto.getChildren().get(1).getStrings().get(2)).isEqualTo("FAKE");
    }

    @Test
    void should_create_object_from_factory(
            @Read("/read/dto.json") ObjectFactory<TestDto> factory
    ) {
        TestDto dto = factory.createWith(Map.of("children[1].strings[2]", "FAKE"));
        assertThat(dto.getChildren().get(1).getStrings().get(2)).isEqualTo("FAKE");
    }

    @Test
    void should_create_objects_from_factory() {
        final List<TestDto> dtos = IntStream.rangeClosed(100, 102)
                .mapToObj(i -> factory.createWith(Map.of("id", i)))
                .collect(toList());
        assertThat(dtos)
                .extracting(TestDto::getId)
                .containsExactly(100, 101, 102);
    }

    @Test
    void should_create_objects_from_factory(
            @Read("/read/dto.json") ObjectFactory<TestDto> factory
    ) {
        final List<TestDto> dtos = IntStream.rangeClosed(100, 102)
                .mapToObj(i -> factory.with("id", i).create())
                .collect(toList());
        assertThat(dtos)
                .extracting(TestDto::getId)
                .containsExactly(100, 101, 102);
    }

}
