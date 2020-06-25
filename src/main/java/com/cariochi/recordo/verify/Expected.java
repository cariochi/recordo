package com.cariochi.recordo.verify;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.FileNotFoundException;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Properties.composeFileName;
import static java.util.Arrays.asList;

@Slf4j
@Builder
public class Expected<T> {

    private final Verify annotation;
    private final Object testInstance;

    public void assertEquals(T actual) {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final String fileName = composeFileName(annotation.value(), testInstance.getClass());
        final String actualJson = jsonConverter.toJson(actual, jsonFilter(annotation));
        try {
            final String expectedJson = Files.readFromFile(fileName);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode(annotation));
        } catch (AssertionError e) {
            String newFileName =
                    new StringBuilder(fileName).insert(fileName.lastIndexOf('/') + 1, "ACTUAL: ").toString();
            Files.writeToFile(actualJson, newFileName)
                    .ifPresent(file -> log.info(
                            e.getMessage() + "\nActual value is saved to file.\n\t* {}",
                            file.getAbsolutePath()
                    ));
            throw e;
        } catch (FileNotFoundException e) {
            final String message = Files.writeToFile(actualJson, fileName)
                    .map(file -> format(
                            e.getMessage() + "\nExpected value is saved.\n\t* {}",
                            file.getAbsolutePath()
                    ))
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
