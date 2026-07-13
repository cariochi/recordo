package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.json.JsonFilter;
import com.cariochi.recordo.core.json.Path;
import com.cariochi.recordo.core.utils.Files;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;

import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

@Setter
@Slf4j
class AsJsonComparator {

    private JsonConverter jsonConverter = JsonConverters.getJsonConverter("", RecordoExtension.getContext());

    @SneakyThrows
    public JSONCompareResult compareAsJson(
            Object actualObject,
            String expectedFileName,
            JsonFilter jsonFilter,
            JSONCompareMode compareMode
    ) {
        final String actualJson = jsonConverter.toJson(actualObject, jsonFilter);
        if (Files.exists(expectedFileName)) {
            final String expectedContent = Files.readString(expectedFileName);
            final String expectedJson = Files.isYaml(expectedFileName)
                    ? jsonConverter.yamlToJson(expectedContent)
                    : expectedContent;
            final JSONCompareResult result = compareJSON(expectedJson, actualJson, new JsonFilterComparator(compareMode, jsonFilter));
            if (result.failed()) {
                String actual = Files.isYaml(expectedFileName)
                        ? jsonConverter.toYaml(actualObject, jsonFilter)
                        : actualJson;
                Files.write(actual, actualFileName(expectedFileName))
                        .ifPresent(file -> log.info(result.getMessage() + "\nActual value is saved to file://{}", file));
            }
            return result;
        } else {
            String content = Files.isYaml(expectedFileName)
                    ? jsonConverter.toYaml(actualObject, jsonFilter)
                    : actualJson;
            Files.write(content, expectedFileName)
                    .ifPresent(file -> log.info("\nExpected value is saved to file://{}", file));
            return failed();
        }
    }

    private String actualFileName(String expectedFileName) {
        return new StringBuilder(expectedFileName)
                .insert(expectedFileName.lastIndexOf('/') + 1, "ACTUAL/")
                .toString();
    }

    private JSONCompareResult failed() {
        final JSONCompareResult jsonCompareResult = new JSONCompareResult();
        jsonCompareResult.fail("Expected value file is absent");
        return jsonCompareResult;
    }

    private static class JsonFilterComparator extends DefaultComparator {

        private final JsonFilter jsonFilter;

        public JsonFilterComparator(JSONCompareMode mode, JsonFilter jsonFilter) {
            super(mode);
            this.jsonFilter = jsonFilter;
        }

        @Override
        public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result) throws JSONException {
            final Path path = new Path(prefix);
            if (jsonFilter.shouldInclude(path)) {
                super.compareValues(prefix, expectedValue, actualValue, result);
            } else {
                result.passed();
            }
        }
    }
}
