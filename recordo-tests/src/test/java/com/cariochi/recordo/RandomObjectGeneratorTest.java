package com.cariochi.recordo;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.RandomObjectGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.cariochi.reflecto.types.Types.listOf;
import static com.cariochi.reflecto.types.Types.mapOf;
import static com.cariochi.reflecto.types.Types.setOf;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Slf4j
@ExtendWith(RecordoExtension.class)
class RandomObjectGeneratorTest {

    private final RandomObjectGenerator generator = new RandomObjectGenerator();
    private final JsonConverter jsonConverter = new JsonConverter();

    private static Stream<Arguments> getTypes() {
        return Stream.of(
                arguments(TestObject.class),
                arguments(listOf(TestObject.class)),
                arguments(setOf(TestObject.class)),
                arguments(mapOf(String.class, TestObject.class)),
                arguments(TestObject[].class)
        );
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void should_generate_object(Type type) {
        final Object generated = generator.generateInstance(type);
        final String json = jsonConverter.toJson(generated);
        final Object parsed = jsonConverter.fromJson(json, type);
        final String json2 = jsonConverter.toJson(parsed);
        assertThat(json).isEqualTo(json2);
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

        private ChildObject object;
        private LocalDate localDate;
        private LocalDateTime localDateTime;
        private Instant instant;
        private Enum anEnum;

        private int[] array0fIntegers;
        private String[] arrayOfStrings;
        private ChildObject[] arrayOfObjects;

        private List<ChildObject> listOfObjects;
        private Set<ChildObject> setOfObjects;
        private Map<String, ChildObject> mapOfObjects;

        private LinkedHashMap<String, Integer> linkedHashMap;
        private TreeSet<Double> treeSet;
        private Map<String, List<ChildObject>> mapOfLists;
        private Set<Enum> setOfEnums;

    }

    @Data
    @JsonInclude(NON_EMPTY)
    public static class ChildObject {

        private String child;

        private Collection<String> strings;

    }

    public enum Enum {
        FIRST,
        SECOND
    }

}
