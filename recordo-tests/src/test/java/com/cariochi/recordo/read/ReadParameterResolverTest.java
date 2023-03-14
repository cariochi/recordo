package com.cariochi.recordo.read;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.main.dto.TestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(RecordoExtension.class)
class ReadParameterResolverTest {

    private static final TestDto EXPECTED_DTO = TestDto.dto(1).withChild(TestDto.dto(2)).withChild(TestDto.dto(3));
    private static final List<TestDto> EXPECTED_LIST = asList(
            TestDto.dto(1).withChild(TestDto.dto(2)).withChild(TestDto.dto(3)),
            TestDto.dto(4).withChild(TestDto.dto(5)).withChild(TestDto.dto(6))
    );


    @EnableRecordo
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new StdDateFormat());

    @Test
    void given(
            @Read("/read/dto.json") TestDto dto
    ) {
        assertAsJson(dto).isEqualTo("/read/dto.json");
    }

    @Test
    void given_factory(
            @Read("/read/dto.json") ObjectFactory<TestDto> factory
    ) {
        TestDto dto = factory.create();
        assertAsJson(dto).isEqualTo("/read/dto.json");
    }

    @Test
    void given_list(
            @Read("/read/list.json") List<TestDto> list
    ) {
        assertAsJson(list).isEqualTo("/read/list.json");
    }

    @Test
    @SneakyThrows
    void given_string(
            @Read("/read/string.json") String string
    ) {
        final TestDto value = objectMapper.readValue(string, TestDto.class);
        assertAsJson(value).isEqualTo("/read/dto.json");
    }

    @Test
    @SneakyThrows
    void given_bytes(
            @Read("/read/string.json") byte[] bytes
    ) {
        final TestDto value = objectMapper.readValue(bytes, TestDto.class);
        assertAsJson(value).isEqualTo("/read/dto.json");
    }

    @Test
    void generated_json_test(
            @Read("/read/generated_dto.json") TestDto dto,
            @Read("/read/generated_list.json") List<TestDto> givenList
    ) {
        assertAsJson(dto).isEqualTo("/read/generated_dto.json");
        assertAsJson(givenList).isEqualTo("/read/generated_list.json");
    }

    @Test
    void given_multiple(
            @Read("/read/dto.json") TestDto dto,
            @Read("/read/list.json") List<TestDto> list
    ) {
        assertEquals(EXPECTED_DTO, dto);
        assertEquals(EXPECTED_LIST, list);
    }

}
