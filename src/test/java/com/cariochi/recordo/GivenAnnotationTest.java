package com.cariochi.recordo;

import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.RecordoJsonConverter;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.junit5.RecordoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.TestPojo.pojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(RecordoExtension.class)
class GivenAnnotationTest {

    private static final TestPojo EXPECTED_OBJECT = pojo(1).withChild(pojo(2)).withChild(pojo(3));
    private static final List<TestPojo> EXPECTED_LIST = asList(
            pojo(1).withChild(pojo(2)).withChild(pojo(3)),
            pojo(4).withChild(pojo(5)).withChild(pojo(6))
    );

    @RecordoJsonConverter
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new StdDateFormat());

    private TestPojo object;
    private List<TestPojo> list;

    @Test
    @Given("object")
    @Verify("object")
    void given() {
        assertEquals(EXPECTED_OBJECT, object);
    }

    @Test
    @Given("list")
    @Verify("list")
    void given_list() {
        assertEquals(EXPECTED_LIST, list);
    }

    @Test
    @Given("object")
    @Given("list")
    @Verify("object")
    @Verify("list")
    void create_empty_json() {
    }

    @Test
    @Given(value = "object", file = "{TEST_CLASS_FULL_NAME}/custom-object-file.json")
    @Given(value = "list", file = "{TEST_CLASS_FULL_NAME}/custom-list-file.json")
    void given_multiple() {
        assertEquals(EXPECTED_OBJECT, object);
        assertEquals(EXPECTED_LIST, list);
    }

}
