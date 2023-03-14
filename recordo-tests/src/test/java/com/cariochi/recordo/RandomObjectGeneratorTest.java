package com.cariochi.recordo;

import com.cariochi.recordo.core.RandomObjectGenerator;
import com.cariochi.recordo.core.RecordoExtension;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.cariochi.reflecto.types.Types.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(RecordoExtension.class)
class RandomObjectGeneratorTest {

    private final RandomObjectGenerator generator = new RandomObjectGenerator();

    @Test
    void should_generate_objects() {
        assertThat(generate(TestObject.class)).isNotNull();
        assertThat((List<?>) generate(listOf(TestObject.class))).isNotEmpty();
        assertThat((Set<?>) generate(setOf(TestObject.class))).isNotEmpty();
        assertThat((Map<?, ?>) generate(mapOf(String.class, TestObject.class))).isNotEmpty();
        assertThat((List<?>) generate(listOf(mapOf(String.class, TestObject.class)))).isNotEmpty();
        assertThat(generate(TestObject[].class)).isNotNull();
    }

    private Object generate(Type type) {
        return generator.generateInstance(type, 2);
    }

    @Data
    @JsonInclude(NON_EMPTY)
    public static class TestObject {

        private String string;

        private Integer integerObject;
        private int integerPrimitive;
        private Long longObject;
        private long longPrimitive;
        private Double doubleObject;
        private double doublePrimitive;
        private Boolean booleanObject;
        private boolean booleanPrimitive;

        private TestObject object;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private Instant instant;
        private Enum anEnum;

        private int[] array0fIntegers;
        private String[] arrayOfStrings;
        private TestObject[] arrayOfObjects;

        private List<TestObject> listOfObjects;
        private Set<TestObject> setOfObjects;
        private Map<String, TestObject> mapOfObjects;

        private LinkedHashMap<String, Integer> linkedHashMap;
        private TreeSet<Double> treeSet;
        private Map<String, List<TestObject>> mapOfLists;
        private Set<Enum> setOfEnums;

    }

    public enum Enum {
        FIRST, SECOND
    }

}
