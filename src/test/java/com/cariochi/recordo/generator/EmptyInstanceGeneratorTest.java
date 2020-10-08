package com.cariochi.recordo.generator;

import com.cariochi.recordo.RecordoExtension;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.cariochi.recordo.assertions.RecordoAssertion.assertAsJson;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class EmptyInstanceGeneratorTest {

    private final EmptyInstanceGenerator generator = new EmptyInstanceGenerator();

    @Test
    void should_generate_object() {
        TestObject object = (TestObject) generator.createInstance(TestObject.class, 2);

        assertThat(object)
                .extracting(TestObject::getLocalDate, TestObject::getLocalDateTime, TestObject::getInstant)
                .doesNotContainNull();

        assertAsJson(object)
                .excluding("localDate", "localDateTime", "instant")
                .isEqualTo("/generator/test_object.json");
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class TestObject {
        private String string;
        private Integer integer;
        private Double aDouble;
        private Boolean aBoolean;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private Instant instant;
        private Enum anEnum;
        private int anInt;
        private boolean bool;
        private int[] ints;
        private String[] strings;
        private TestObject object;
        private TestObject[] objects;
        private List<TestObject> objectList;
        private Set<TestObject> objectSet;
        private Map<String, TestObject> map;
        private LinkedHashMap<String, Integer> linkedHashMap;
        private TreeSet<Double> treeSet;
        private Map<String, List<TestObject>> stringListMap;
        private Set<Enum> enumSet;
    }

    public enum Enum {
        FIRST, SECOND
    }
}
