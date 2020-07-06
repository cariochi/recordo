package com.cariochi.recordo.verify;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.nio.file.NoSuchFileException;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Arrays.asList;

@Slf4j
@RequiredArgsConstructor
public class Expected<T> {

    private final Verify annotation;
    private final JsonConverter jsonConverter;

    public void assertEquals(T actual) {
        final String fileName = annotation.value();
        final JsonPropertyFilter jsonFilter = jsonFilter(annotation);
        final String actualJson = jsonConverter.toJson(actual, jsonFilter);
        try {
            final String expectedJson = Files.read(fileName);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode(annotation));
        } catch (AssertionError e) {
            String newFileName =
                    new StringBuilder(fileName).insert(fileName.lastIndexOf('/') + 1, "ACTUAL: ").toString();
            Files.write(actualJson, newFileName)
                    .ifPresent(file -> log.info(e.getMessage() + "\nActual value is saved: {}", file));
            throw e;
        } catch (NoSuchFileException e) {
            final String message = Files.write(actualJson, fileName)
                    .map(file -> format(e.getMessage() + "\nExpected value is saved: {}", file))
                    .orElse(e.getMessage());
            throw new AssertionError(message);
        } catch (JSONException e) {
            throw new RecordoError(e);
        }
    }

    private JsonPropertyFilter jsonFilter(Verify verify) {
        return new JsonPropertyFilter(asList(verify.included()), asList(verify.excluded()));
    }

    private JSONCompareMode compareMode(Verify verify) {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == verify.extensible())
                .filter(mode -> mode.hasStrictOrder() == verify.strictOrder())
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }
}
