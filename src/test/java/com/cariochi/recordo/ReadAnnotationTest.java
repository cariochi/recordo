package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.RecordoAssertion.assertAsJson;
import static com.cariochi.recordo.dto.TestDto.dto;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldNameConstants
@ExtendWith(RecordoExtension.class)
class ReadAnnotationTest {

    private static final TestDto EXPECTED_DTO = dto(1).withChild(dto(2)).withChild(dto(3));
    private static final List<TestDto> EXPECTED_LIST = asList(
            dto(1).withChild(dto(2)).withChild(dto(3)),
            dto(4).withChild(dto(5)).withChild(dto(6))
    );

    @EnableRecordo
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new StdDateFormat());

    @Test
    void given(
            @Read("/given_annotation_test/dto.json") TestDto dto
    ) {
        assertAsJson(dto).isEqualTo("/given_annotation_test/dto.json");
    }

    @Test
    void given_list(
            @Read("/given_annotation_test/list.json") List<TestDto> list
    ) {
        assertAsJson(list).isEqualTo("/given_annotation_test/list.json");
    }

    @Test
    @SneakyThrows
    void given_string(
            @Read("/given_annotation_test/string.json") String string
    ) {
        final TestDto value = objectMapper.readValue(string, TestDto.class);
        assertAsJson(value).isEqualTo("/given_annotation_test/dto.json");
    }

    @Test
    void generated_json_test(
            @Read("/given_annotation_test/generated_dto.json") TestDto dto,
            @Read("/given_annotation_test/generated_list.json") List<TestDto> givenList
    ) {
        assertAsJson(dto).isEqualTo("/given_annotation_test/generated_dto.json");
        assertAsJson(givenList).isEqualTo("/given_annotation_test/generated_list.json");
    }

    @Test
    void given_multiple(
            @Read("/given_annotation_test/dto.json") TestDto dto,
            @Read("/given_annotation_test/list.json") List<TestDto> list
    ) {
        assertEquals(EXPECTED_DTO, dto);
        assertEquals(EXPECTED_LIST, list);
    }

}
