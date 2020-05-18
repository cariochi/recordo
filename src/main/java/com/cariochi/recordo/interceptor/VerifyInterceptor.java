package com.cariochi.recordo.interceptor;

import com.cariochi.recordo.RecordoException;
import com.cariochi.recordo.Verifies;
import com.cariochi.recordo.Verify;
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

import static com.cariochi.recordo.utils.ReflectionUtils.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

public class VerifyInterceptor implements BeforeTestInterceptor, AfterTestInterceptor {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(VerifyInterceptor.class);
    private final JsonConverter jsonConverter;

    public VerifyInterceptor(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void beforeTest(Object testInstance, Method method) {
        findVerifyAnnotations(method)
                .forEach(verify -> clearField(testInstance, verify));
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        ExceptionsSuppressor.of(AssertionError.class).executeAll(
                findVerifyAnnotations(method).map(verify -> () -> assertEquals(testInstance, verify, method))
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

    private void assertEquals(Object testInstance, Verify verify, Method method) {

        final Object actual = readField(testInstance, verify.value());

        if (actual == null) {
            throw new AssertionError(format("Actual '%s' value should not be null", verify.value()));
        }

        final String fileName = fileName(verify, method);
        final String actualJson = jsonConverter.toJson(actual, jsonFilter(verify));
        try {
            final String expectedJson = readJsonFromFile(fileName);
            log.debug("Asserting expected \n{} is equals to actual \n{}", expectedJson, actualJson);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode(verify));
            log.info("Asserted actual `{}` value equals to expected in `{}`", verify.value(), fileName);
        } catch (AssertionError | IOException e) {
            final String message = writeJsonToFile(actualJson, fileName)
                    .map(
                            file -> format(
                                    "\n'%s' assertion failed: %s" +
                                    "\nExpected '%s' value file was created.",
                                    verify.value(), e.getMessage(), verify.value()
                            )
                    )
                    .orElse(e.getMessage());
            throw new AssertionError(message);
        } catch (JSONException e) {
            throw new RecordoException(e);
        }
    }

    private String fileName(Verify verify, Method method) {
        final String fileNamePattern = Optional.of(verify.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::verifyFileNamePattern);

        return Files.fileName(fileNamePattern, method, verify.value());
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
                .orElseGet(() -> findAnnotation(method, Verify.class));
    }
}
