package com.cariochi.recordo.json;

import com.cariochi.recordo.dto.TestDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.cariochi.recordo.dto.TestDto.dto;
import static java.util.Arrays.asList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JacksonConverterTest {

    private static final TestDto GIVEN_OBJECT = dto(1).withChild(dto(2));

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

    private final JsonConverter jsonConverter = new JacksonConverter();

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
