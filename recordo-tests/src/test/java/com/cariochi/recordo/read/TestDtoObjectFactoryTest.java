package com.cariochi.recordo.read;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.main.dto.TestDto;
import com.cariochi.recordo.read.factories.TestDtoObjectFactory;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class TestDtoObjectFactoryTest {

    @EnableRecordo
    private TestDtoObjectFactory objectFactory;

    @Test
    void should_get_object() {
        final TestDto dto = objectFactory
                .text("Hello, world!")
                .allChildrenStrings("FAKE")
                .testDto(33);

        assertThat(dto)
                .extracting(TestDto::getId, TestDto::getText)
                .contains(33, "Hello, world!");

        dto.getChildren().stream()
                .flatMap(child -> Stream.of(child.getStrings()))
                .forEach(s -> assertThat(s).isEqualTo("FAKE"));
    }

    @Test
    void should_get_dtos() {
        final List<TestDto> dtos = objectFactory
                .text("Hello, world!")
                .allChildrenStrings("FAKE")
                .testDtoList();

        assertThat(dtos)
                .hasSize(3)
                .extracting(TestDto::getText)
                .containsOnly("Hello, world!");

        dtos.stream()
                .flatMap(dto -> dto.getChildren().stream())
                .flatMap(child -> Stream.of(child.getStrings()))
                .forEach(s -> assertThat(s).isEqualTo("FAKE"));
    }

    @Test
    void should_work_with_default_methods() {
        final TestDto defaultTestDto = objectFactory.defaultTestDto();
        assertThat(defaultTestDto)
                .extracting(TestDto::getId, TestDto::getText)
                .contains(101, "DEFAULT");
    }

}
