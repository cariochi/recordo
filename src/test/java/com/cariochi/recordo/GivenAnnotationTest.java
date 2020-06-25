package com.cariochi.recordo;

import com.cariochi.recordo.annotation.EnableRecordo;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Resources;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.junit5.RecordoExtension;
import com.cariochi.recordo.verify.Expected;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.TestObject.pojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldNameConstants
@ExtendWith(RecordoExtension.class)
@Resources("/given_annotation_test")
class GivenAnnotationTest {

    private static final TestObject EXPECTED_OBJECT = pojo(1).withChild(pojo(2)).withChild(pojo(3));
    private static final List<TestObject> EXPECTED_LIST = asList(
            pojo(1).withChild(pojo(2)).withChild(pojo(3)),
            pojo(4).withChild(pojo(5)).withChild(pojo(6))
    );

    @EnableRecordo
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new StdDateFormat());


    @Given("/list.json")
    private List<TestObject> list;

    @Given("/object.json")
    private TestObject object;

    private Expected<TestObject> expectedObject;

    @Verify("/list.json")
    private Expected<List<TestObject>> expectedList;

    @Test
    @Verify(value = "/object.json", field = Fields.expectedObject)
    void given() {
        expectedObject.assertEquals(object);
    }

    @Test
    void given_list() {
        expectedList.assertEquals(list);
    }

    @Test
    void given_string(
            @Given("/string.json") String string,
            @Verify("/object.json") Expected<TestObject> expected
    ) throws JsonProcessingException {
        final TestObject value = objectMapper.readValue(string, TestObject.class);
        expected.assertEquals(value);
    }

    @Test
    void create_empty_json(@Given("/object.json") TestObject pojo,
                           @Given("/list.json") List<TestObject> givenList,
                           @Verify("/object.json") Expected<TestObject> objectExpected,
                           @Verify("/list.json") Expected<List<TestObject>> listExpected
    ) {
        objectExpected.assertEquals(pojo);
        listExpected.assertEquals(givenList);
    }

    @Test
    void given_multiple(
            @Given("/object.json") TestObject object,
            @Given("/list.json") List<TestObject> list
    ) {
        assertEquals(EXPECTED_OBJECT, object);
        assertEquals(EXPECTED_LIST, list);
    }

}
