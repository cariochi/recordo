package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import com.cariochi.recordo.given.Assertion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.dto.TestDto.dto;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldNameConstants
@ExtendWith(RecordoExtension.class)
class GivenAnnotationTest {

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
            @Given("/given_annotation_test/dto.json") TestDto dto,
            @Given("/given_annotation_test/dto.json") Assertion<TestDto> assertion
    ) {
        assertion.assertAsExpected(dto);
    }

    @Test
    void given_list(
            @Given("/given_annotation_test/list.json") List<TestDto> list,
            @Given("/given_annotation_test/list.json") Assertion<List<TestDto>> assertion
    ) {
        assertion.assertAsExpected(list);
    }

    @Test
    @SneakyThrows
    void given_string(
            @Given("/given_annotation_test/string.json") String string,
            @Given("/given_annotation_test/dto.json") Assertion<TestDto> assertion
    ) {
        final TestDto value = objectMapper.readValue(string, TestDto.class);
        assertion.assertAsExpected(value);
    }

    @Test
    void generated_json_test(
            @Given("/given_annotation_test/generated_dto.json") TestDto dto,
            @Given("/given_annotation_test/generated_list.json") List<TestDto> givenList,
            @Given("/given_annotation_test/generated_dto.json") Assertion<TestDto> dtoAssertion,
            @Given("/given_annotation_test/generated_list.json") Assertion<List<TestDto>> listAssertion
    ) {
        dtoAssertion.assertAsExpected(dto);
        listAssertion.assertAsExpected(givenList);
    }

    @Test
    void given_multiple(
            @Given("/given_annotation_test/dto.json") TestDto dto,
            @Given("/given_annotation_test/list.json") List<TestDto> list
    ) {
        assertEquals(EXPECTED_DTO, dto);
        assertEquals(EXPECTED_LIST, list);
    }

}
