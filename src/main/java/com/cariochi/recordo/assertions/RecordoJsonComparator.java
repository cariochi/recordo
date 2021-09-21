package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

@Slf4j
class RecordoJsonComparator<T> {

    @Setter
    private JsonConverter jsonConverter = new JacksonConverter();

    @SneakyThrows
    public JSONCompareResult compareAsJson(
            Object actualObject,
            String expectedFileName,
            JsonPropertyFilter jsonFilter,
            JSONCompareMode compareMode
    ) {
        final String actualJson = jsonConverter.toJson(actualObject, jsonFilter);
        if (Files.exists(expectedFileName)) {
            final String expectedJson = Files.read(expectedFileName);
            final JSONCompareResult result = compareJSON(expectedJson, actualJson, compareMode);
            if (result.failed()) {
                Files.write(actualJson, actualFileName(expectedFileName))
                        .ifPresent(file -> log.info(result.getMessage() + "\nActual value is saved to file://{}", file));
            }
            return result;
        } else {
            Files.write(actualJson, expectedFileName)
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

}
