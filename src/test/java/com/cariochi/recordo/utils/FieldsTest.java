package com.cariochi.recordo.utils;

import com.cariochi.recordo.TestPojo;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.cariochi.recordo.TestPojo.pojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldsTest {

    @Test
    void test_get_field() {
        final TestPojo testPojo = pojo(1).withParent(pojo(2));
        final TargetField field = Fields.of(testPojo).get("parent.id");
        final Integer id = field.getValue();
        assertEquals(id, 2);
    }

    @Test
    void test_get_field_2() {
        final TestPojo testPojo = pojo(1).withParent(pojo(2));
        final TargetField field = Fields.of(testPojo).get("parent").get("id");
        final Integer id = field.getValue();
        assertEquals(id, 2);
    }

    @Test
    void test_get_field_3() {
        final TestPojo testPojo = pojo(1).withParent(pojo(2));
        final Fields fields = Fields.of(testPojo).get("parent").fields();
        final Integer id = fields.get("id").getValue();
        assertEquals(id, 2);
    }

    @Test
    void test_get_field_4() {
        final TestPojo testPojo = pojo(1).withChild(pojo(2));
        final List<TestPojo> children = Fields.of(testPojo).get("children").getValue();
        final Integer id = Fields.of(children.get(0)).get("id").getValue();
        assertEquals(id, 2);
    }
}
