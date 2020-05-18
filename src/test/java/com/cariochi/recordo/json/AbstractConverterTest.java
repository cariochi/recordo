package com.cariochi.recordo.json;

import com.cariochi.recordo.TestPojo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.cariochi.recordo.TestPojo.pojo;
import static java.util.Arrays.asList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@RequiredArgsConstructor
abstract class AbstractConverterTest {

    private static final TestPojo GIVEN_OBJECT = pojo(1).withChild(pojo(2));

    private static final String EXPECTED_JSON = "{" +
                                                "    \"id\" : 1," +
                                                "    \"text\" : \"Test Object 1\"," +
                                                "    \"children\" : [" +
                                                "        {" +
                                                "            \"id\" : 2," +
                                                "            \"text\" : \"Test Object 2\"" +
                                                "        }" +
                                                "    ]" +
                                                "}";

    private final JsonConverter jsonConverter;

    @Test
    @SneakyThrows
    void should_remove_fields_by_include() {
        // given
        final JsonPropertyFilter jsonPropertyFilter = new JsonPropertyFilter(
                asList("id", "text", "children.id", "children.text"),
                asList()
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonPropertyFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }

    @Test
    @SneakyThrows
    void should_remove_fields_by_exclude() {
        // given
        final JsonPropertyFilter jsonPropertyFilter = new JsonPropertyFilter(
                asList(),
                asList("date", "strings", "children.date", "children.strings", "children.children")
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonPropertyFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }

    @Test
    @SneakyThrows
    void should_remove_fields_by_include_and_exclude() {
        // given
        final JsonPropertyFilter jsonPropertyFilter = new JsonPropertyFilter(
                asList("id", "text", "date", "children.id", "children.text", "children.date"),
                asList("date", "strings", "children.date", "children.strings", "children.children")
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonPropertyFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }
}
