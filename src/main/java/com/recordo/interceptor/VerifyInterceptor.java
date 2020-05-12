package com.recordo.interceptor;

import com.recordo.Verifies;
import com.recordo.Verify;
import com.recordo.json.JsonConverter;
import com.recordo.json.JsonPropertyFilter;
import com.recordo.utils.Files;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static com.recordo.utils.ReflectionUtils.readField;
import static com.recordo.utils.ReflectionUtils.writeField;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

@Slf4j
public class VerifyInterceptor extends AbstractInterceptor {

    public VerifyInterceptor(String rootFolder, JsonConverter jsonConverter) {
        super(jsonConverter, new Files(rootFolder));
    }

    @Override
    public void beforeTest(Object testInstance, Method method) {
        findVerifyAnnotations(method)
                .forEach(verify -> clearField(testInstance, verify));
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        final List<AssertionError> errors = findVerifyAnnotations(method).stream()
                .map(annotation ->
                        assertEquals(testInstance, annotation, method))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        if (!errors.isEmpty()) {
            if (errors.size() == 1) {
                throw errors.get(0);
            } else {
                final String message = errors.stream().map(AssertionError::getMessage).collect(joining("\n"));
                throw new AssertionError(message);
            }
        }
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

    private Optional<AssertionError> assertEquals(Object testInstance, Verify verify, Method method) {

        final Object actual = readField(testInstance, verify.value());
        final JsonPropertyFilter jsonPropertyFilter = JsonPropertyFilter.builder()
                .included(asList(verify.included()))
                .excluded(asList(verify.excluded()))
                .build();
        final String actualJson = jsonConverter.toJson(actual, jsonPropertyFilter);

        final String fileName = Optional.of(verify.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> fileName(method, verify.value()));
        try {

            files.readFromFile(fileName)
                    .map(expectedJson -> assertJson(expectedJson, actualJson, verify))
                    .orElseThrow(() -> new FileNotFoundException(format("File not found: %s", fileName)));

            log.info("Assert actual `{}` value equals to expected in `{}`", verify.value(), fileName);

            return Optional.empty();

        } catch (AssertionError | FileNotFoundException e) {

            final String message = files.writeToFile(actualJson, fileName)
                    .map(file -> format(
                            "\n'%s' Assertion failed: \n%s\nExpected object was recorded to 'file://%s'.",
                            verify.value(),
                            e.getMessage(),
                            file.getAbsolutePath()
                    ))
                    .orElse(e.getMessage());

            return Optional.of(new AssertionError(message));
        }
    }

    @SneakyThrows
    private String assertJson(String expectedJson, String actualJson, Verify verify) {
        log.debug("Assert expected \n{} is equals to actual \n{}", expectedJson, actualJson);
        JSONAssert.assertEquals(expectedJson, actualJson, compareMode(verify));
        return actualJson;
    }

    private JSONCompareMode compareMode(Verify verify) {
        return Stream.of(JSONCompareMode.values())
                .filter(mode -> mode.isExtensible() == verify.extensible())
                .filter(mode -> mode.hasStrictOrder() == verify.strictOrder())
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Compare mode not found"));
    }

    private List<Verify> findVerifyAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Verifies.class, true, true))
                .map(Verifies::value)
                .map(Arrays::asList)
                .orElseGet(() -> findAnnotation(method, Verify.class));
    }
}
