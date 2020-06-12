package com.cariochi.recordo.verify;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.Files;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Properties.fileName;
import static com.cariochi.recordo.utils.Properties.verifyFileNamePattern;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

public class Verifier {

    private static final Logger log = getLogger(Verifier.class);

    private final Files files = new Files();

    public void verify(Object actual, Verify verify, Object testInstance, Method method, String parameter) {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final Class<?> testClass = testInstance.getClass();
        final String parameterName = Optional.of(verify.value())
                .filter(StringUtils::isNotBlank)
                .orElse(parameter);
        final String fileNamePattern = verifyFileNamePattern(verify.file());
        final String fileName = fileName(fileNamePattern, testClass, method.getName(), parameterName);
        final String actualJson = jsonConverter.toJson(actual, jsonFilter(verify));
        try {
            final String expectedJson = files.readFromFile(fileName);
            log.debug("Verifying '{}'", parameterName);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode(verify));
            log.info("Actual '{}' value equals to expected.\n\t* {}", parameterName, files.filePath(fileName));
        } catch (AssertionError e) {
            String newFileName = new StringBuilder(fileName).insert(fileName.lastIndexOf('/') + 1, "new-").toString();
            files.writeToFile(actualJson, newFileName)
                    .ifPresent(file -> log.info(
                            e.getMessage() + "\nActual '{}' value is saved to file.\n\t* {}",
                            parameterName,
                            file.getAbsolutePath()
                    ));
            throw e;
        } catch (FileNotFoundException e) {
            final String message = files.writeToFile(actualJson, fileName)
                    .map(file -> format(
                            e.getMessage() + "\nExpected '{}' value is saved.\n\t* {}",
                            parameterName,
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
