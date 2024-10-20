package com.cariochi.recordo.json;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonFilter;
import com.cariochi.recordo.main.dto.TestDto;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonConverterTest {

    private static final TestDto GIVEN_OBJECT = TestDto.dto(1).withChild(TestDto.dto(2));

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

    private final JsonConverter jsonConverter = new JsonConverter();

    @Test
    @SneakyThrows
    void should_remove_fields_by_include() {
        // given
        final JsonFilter jsonFilter = new JsonFilter(
                List.of("id", "text", "children[*].id", "children[*].text"),
                List.of()
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }

    @Test
    @SneakyThrows
    void should_remove_fields_by_exclude() {
        // given
        final JsonFilter jsonFilter = new JsonFilter(
                List.of(),
                List.of("date", "strings", "children[*].date", "children[*].strings", "children[*].children")
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }

    @Test
    @SneakyThrows
    void should_remove_fields_by_include_and_exclude() {
        // given
        final JsonFilter jsonFilter = new JsonFilter(
                List.of("id", "text", "date", "children[*].id", "children[*].text", "children[*].date"),
                List.of("date", "strings", "children[*].date", "children[*].strings", "children[*].children")
        );

        // when
        final String result = jsonConverter.toJson(GIVEN_OBJECT, jsonFilter);

        // then
        assertEquals(EXPECTED_JSON, result, JSONCompareMode.STRICT);
    }
}
