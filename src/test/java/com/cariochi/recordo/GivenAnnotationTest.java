package com.cariochi.recordo;

import com.cariochi.recordo.dto.TestDto;
import com.cariochi.recordo.given.Given;
import com.cariochi.recordo.verify.Expected;
import com.cariochi.recordo.verify.Verify;
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


    @Given("/given_annotation_test/dto.json")
    private TestDto givenDto;

    @Given("/given_annotation_test/list.json")
    private List<TestDto> givenList;

    @Test
    void given(@Verify("/given_annotation_test/dto.json") Expected<TestDto> expected) {
        expected.assertEquals(givenDto);
    }

    @Test
    void given_list(@Verify("/given_annotation_test/list.json") Expected<List<TestDto>> expected) {
        expected.assertEquals(givenList);
    }

    @Test
    @SneakyThrows
    void given_string(
            @Given("/given_annotation_test/string.json") String string,
            @Verify("/given_annotation_test/dto.json") Expected<TestDto> expected
    ) {
        final TestDto value = objectMapper.readValue(string, TestDto.class);
        expected.assertEquals(value);
    }

    @Test
    void generated_json_test(@Given("/given_annotation_test/generated_dto.json") TestDto dto,
                           @Given("/given_annotation_test/generated_list.json") List<TestDto> givenList,
                           @Verify("/given_annotation_test/generated_dto.json") Expected<TestDto> expectedDto,
                           @Verify("/given_annotation_test/generated_list.json") Expected<List<TestDto>> expectedList
    ) {
        expectedDto.assertEquals(dto);
        expectedList.assertEquals(givenList);
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
