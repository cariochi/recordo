package com.cariochi.recordo;

import com.cariochi.recordo.annotation.EnableRecordo;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.junit5.RecordoExtension;
import com.cariochi.recordo.verify.Verifier;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @EnableRecordo
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDateFormat(new StdDateFormat());


    @Given(file = "/{package}/{class}/given_list.json")
    private List<TestPojo> list;

    @Given(file = "/{package}/{class}/given_object.json")
    private TestPojo object;

    @Test
    @Verify("object")
    void given() {
        assertEquals(EXPECTED_OBJECT, object);
    }

    @Test
    @Verify("list")
    void given_list() {
        assertEquals(EXPECTED_LIST, list);
    }

    @Test
    void given_string(
            @Given(file = "/{package}/{class}/given_string.json") String string,
            @Verify("object") Verifier actual
    ) throws JsonProcessingException {
        final TestPojo value = objectMapper.readValue(string, TestPojo.class);
        actual.verify(value);
    }

    @Test
    void create_empty_json(@Given("object") TestPojo pojo,
                           @Given("list") List<TestPojo> givenList,
                           @Verify("object") Verifier objectVerifier,
                           @Verify("list") Verifier listVerifier
    ) {
        // test logic
        objectVerifier.verify(pojo);
        listVerifier.verify(givenList);
    }

    @Test
    void given_multiple(
            @Given(file = "/{package}/{class}/given_object.json") TestPojo object,
            @Given(file = "/{package}/{class}/given_list.json") List<TestPojo> list
    ) {
        assertEquals(EXPECTED_OBJECT, object);
        assertEquals(EXPECTED_LIST, list);
    }

}
