package com.recordo;

import com.recordo.junit5.RecordoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.recordo.TestPojo.pojo;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(RecordoExtension.class)
class GivenAnnotationTest {

    private static final TestPojo EXPECTED_OBJECT = pojo(1).withChild(pojo(2)).withChild(pojo(3));
    private static final List<TestPojo> EXPECTED_LIST = asList(
            pojo(1).withChild(pojo(2)).withChild(pojo(3)),
            pojo(4).withChild(pojo(5)).withChild(pojo(6))
    );

    private TestPojo object;
    private List<TestPojo> list;

    @Test
    @Given("object")
    void given() {
        assertEquals(EXPECTED_OBJECT, object);
    }

    @Test
    @Given("list")
    void given_list() {
        assertEquals(EXPECTED_LIST, list);
    }

    @Test
    @Given(value = "object", file = "custom-object-file.json")
    @Given(value = "list", file = "custom-list-file.json")
    void given_multiple() {
        assertEquals(EXPECTED_OBJECT, object);
        assertEquals(EXPECTED_LIST, list);
    }

}
