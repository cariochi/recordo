package com.cariochi.recordo.utils;

import com.cariochi.recordo.TestObject;
import com.cariochi.recordo.reflection.Fields;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.cariochi.recordo.TestObject.pojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldsTest {

    @Test
    void should_get_field_of_field_value() {
        final TestObject testObject = pojo(1).withParent(pojo(2));
        final Integer id = Fields.of(testObject).get("parent").get("id").getValue();
        assertEquals(id, 2);
    }

    @Test
    void should_get_fields_of_field_value() {
        final TestObject testObject = pojo(1).withParent(pojo(2));
        final Fields fields = Fields.of(testObject).get("parent").fields();
        final Integer id = fields.get("id").getValue();
        assertEquals(id, 2);
    }

    @Test
    void should_get_field_of_collection() {
        final TestObject testObject = pojo(1).withChild(pojo(2));
        final List<TestObject> children = Fields.of(testObject).get("children").getValue();
        final Integer id = Fields.of(children.get(0)).get("id").getValue();
        assertEquals(id, 2);
    }
}
