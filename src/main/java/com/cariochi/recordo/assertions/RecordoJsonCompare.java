package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Data
class RecordoJsonCompare<T> {

    private JsonConverter jsonConverter = new JacksonConverter();
    private boolean extensible = false;
    private boolean strictOrder = true;
    private final List<String> included = new ArrayList<>();
    private final List<String> excluded = new ArrayList<>();
    private String fileName;
    private T actual;

    RecordoJsonCompare(T actual) {
        this.actual = actual;
    }

    RecordoJsonCompare(String fileName) {
        this.fileName = fileName;
    }

    public RecordoJsonCompare(T actual, String fileName) {
        this.fileName = fileName;
        this.actual = actual;
    }

    @SneakyThrows
    public JSONCompareResult compareJson() {
        final JsonPropertyFilter jsonFilter = new JsonPropertyFilter(included, excluded);
        final String actualJson = jsonConverter.toJson(actual, jsonFilter);
        if (Files.exists(fileName)) {
            final String expectedJson = Files.read(fileName);
            JSONCompareResult result = JSONCompare.compareJSON(expectedJson, actualJson, compareMode());
            if (result.failed()) {
                String newFileName = new StringBuilder(fileName).insert(fileName.lastIndexOf('/') + 1, "ACTUAL/").toString();
                Files.write(actualJson, newFileName)
                        .ifPresent(file -> log.info(result.getMessage() + "\nActual value is saved to file://{}", file));
            }
            return result;
        } else {
            Files.write(actualJson, fileName)
                    .ifPresent(file -> log.info("\nExpected value is saved to file://{}", file));
            final JSONCompareResult jsonCompareResult = new JSONCompareResult();
            jsonCompareResult.fail("Expected value file was absent");
            return jsonCompareResult;
        }
    }

    private JSONCompareMode compareMode() {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == extensible)
                .filter(mode -> mode.hasStrictOrder() == strictOrder)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }
}
