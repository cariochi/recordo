package com.cariochi.recordo.verify;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Verifies;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.ExceptionsSuppressor;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.Properties;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Reflection.*;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;
import static org.slf4j.LoggerFactory.getLogger;

public class VerifyAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    private static final Logger log = getLogger(VerifyAnnotationHandler.class);

    private JsonConverter jsonConverter;

    @Override
    public void beforeTest(Object testInstance, Method method) {
        jsonConverter = JsonConverter.of(testInstance);
        findVerifyAnnotations(method)
                .forEach(verify -> clearField(testInstance, verify));
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        ExceptionsSuppressor.of(AssertionError.class).executeAll(
                findVerifyAnnotations(method).map(verify -> () -> verifyTestResult(verify, method, testInstance))
        );
    }

    private void clearField(Object testInstance, Verify verify) {
        final Object value = readField(testInstance, verify.value());
        if (value != null) {
            if (value instanceof Collection) {
                ((Collection) value).clear();
            } else if (value instanceof Map) {
                ((Map) value).clear();
            } else {
                writeField(testInstance, verify.value(), null);
            }
        }
    }

    private void verifyTestResult(Verify verify, Method method, Object testInstance) {

        final Object actual = readField(testInstance, verify.value());

        if (actual == null) {
            throw new AssertionError(format("Actual '{}' value should not be null", verify.value()));
        }

        final String fileName = fileName(verify, testInstance.getClass(), method);
        final String actualJson = jsonConverter.toJson(actual, jsonFilter(verify));
        try {
            final String expectedJson = readJsonFromFile(fileName);
            log.debug("Asserting expected \n{} is equals to actual \n{}", expectedJson, actualJson);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode(verify));
            log.info("Asserted actual '{}' value equals to expected in '{}'", verify.value(), fileName);
        } catch (AssertionError | IOException e) {
            final String message = writeJsonToFile(actualJson, fileName)
                    .map(
                            file -> format(
                                    "\n'{}' assertion failed: {}" +
                                    "\nExpected '{}' value file was created.",
                                    verify.value(), e.getMessage(), verify.value()
                            )
                    )
                    .orElse(e.getMessage());
            throw new AssertionError(message);
        } catch (JSONException e) {
            throw new RecordoError(e);
        }
    }

    private String fileName(Verify verify, Class<?> testClass, Method method) {
        final String fileNamePattern = Optional.of(verify.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::verifyFileNamePattern);

        return Files.fileName(fileNamePattern, testClass, method, verify.value());
    }

    private JsonPropertyFilter jsonFilter(Verify verify) {
        return new JsonPropertyFilter(asList(verify.included()), asList(verify.excluded()));
    }

    String readJsonFromFile(String fileName) throws IOException {
        return Files.readFromFile(fileName);
    }

    Optional<File> writeJsonToFile(String actualJson, String fileName) {
        return Files.writeToFile(actualJson, fileName);
    }

    private JSONCompareMode compareMode(Verify verify) {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == verify.extensible())
                .filter(mode -> mode.hasStrictOrder() == verify.strictOrder())
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }

    private Stream<Verify> findVerifyAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Verifies.class, true, true))
                .map(Verifies::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Verify.class).map(Stream::of).orElseGet(Stream::empty));
    }
}
