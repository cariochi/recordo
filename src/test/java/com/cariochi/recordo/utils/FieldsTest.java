package com.cariochi.recordo.utils;

import com.cariochi.recordo.dto.TestDto;
import com.cariochi.recordo.utils.reflection.Fields;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.cariochi.recordo.dto.TestDto.dto;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldsTest {

    @Test
    void should_get_field_of_field_value() {
        final TestDto testDto = dto(1).withParent(dto(2));
        final Integer id = Fields.of(testDto).get("parent").get("id").getValue();
        assertEquals(id, 2);
    }

    @Test
    void should_get_fields_of_field_value() {
        final TestDto testDto = dto(1).withParent(dto(2));
        final Fields fields = Fields.of(testDto).get("parent").fields();
        final Integer id = fields.get("id").getValue();
        assertEquals(id, 2);
    }

    @Test
    void should_get_field_of_collection() {
        final TestDto testDto = dto(1).withChild(dto(2));
        final List<TestDto> children = Fields.of(testDto).get("children").getValue();
        final Integer id = Fields.of(children.get(0)).get("id").getValue();
        assertEquals(id, 2);
    }
}
