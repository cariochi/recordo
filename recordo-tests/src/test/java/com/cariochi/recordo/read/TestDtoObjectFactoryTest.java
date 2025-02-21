package com.cariochi.recordo.read;

import com.cariochi.recordo.core.Recordo;
import com.cariochi.recordo.main.dto.TestDto;
import com.cariochi.recordo.read.factories.TestDtoFactory;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TestDtoObjectFactoryTest {

    private final TestDtoFactory objectFactory = Recordo.create(TestDtoFactory.class);

    @Test
    void should_get_object() {
        final TestDto dto = objectFactory
                .withText("Hello, world!")
                .withAllChildrenStrings("FAKE")
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
                .withText("Hello, world!")
                .withAllChildrenStrings("FAKE")
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
        final TestDto dto = objectFactory.withText("Hello, world!").defaultTestDto();
        assertThat(dto)
                .extracting(TestDto::getId, TestDto::getText)
                .contains(101, "Hello, world!");

        final List<TestDto> dtoList = objectFactory.withText("Hello, world!").defaultTestDtoList();
        assertThat(dtoList)
                .extracting(TestDto::getId, TestDto::getText)
                .contains(tuple(101, "Hello, world!"));
    }

}
