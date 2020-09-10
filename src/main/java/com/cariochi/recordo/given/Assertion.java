package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Arrays.asList;

@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor
public class Assertion<T> {

    private final String fileName;
    private final JsonConverter jsonConverter;

    @Setter
    private boolean extensible = false;
    @Setter
    private boolean strictOrder = true;

    private final List<String> included = new ArrayList<>();
    private final List<String> excluded = new ArrayList<>();


    public Assertion<T> included(String... fields) {
        included.clear();
        included.addAll(asList(fields));
        return this;
    }

    public Assertion<T> excluded(String... fields) {
        excluded.clear();
        excluded.addAll(asList(fields));
        return this;
    }

    public void assertAsExpected(T actual) {
        final JsonPropertyFilter jsonFilter = jsonFilter();
        final String actualJson = jsonConverter.toJson(actual, jsonFilter);
        try {
            final String expectedJson = Files.read(fileName);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode());
        } catch (AssertionError e) {
            String newFileName =
                    new StringBuilder(fileName).insert(fileName.lastIndexOf('/') + 1, "ACTUAL/").toString();
            Files.write(actualJson, newFileName)
                    .ifPresent(file -> log.info(e.getMessage() + "\nActual value is saved to file://{}", file));
            throw e;
        } catch (NoSuchFileException e) {
            final String message = Files.write(actualJson, fileName)
                    .map(file -> format(e.getMessage() + "\nExpected value is saved to file://{}", file))
                    .orElse(e.getMessage());
            throw new AssertionError(message);
        } catch (JSONException e) {
            throw new RecordoError(e);
        }
    }

    private JsonPropertyFilter jsonFilter() {
        return new JsonPropertyFilter(included, excluded);
    }

    private JSONCompareMode compareMode() {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == extensible)
                .filter(mode -> mode.hasStrictOrder() == strictOrder)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }
}
