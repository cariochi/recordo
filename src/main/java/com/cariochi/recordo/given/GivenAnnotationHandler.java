package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Givens;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.ExceptionsSuppressor;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Files.readFromFile;
import static com.cariochi.recordo.utils.Files.writeToFile;
import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Reflection.*;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;
import static org.slf4j.LoggerFactory.getLogger;

public class GivenAnnotationHandler implements BeforeTestHandler {

    private static final Logger log = getLogger(GivenAnnotationHandler.class);

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    private JsonConverter jsonConverter;

    @Override
    public void beforeTest(Object testInstance, Method method) {
        jsonConverter = JsonConverter.of(testInstance);
        ExceptionsSuppressor.of(RecordoError.class).executeAll(
                findGivenAnnotations(method).map(given -> () -> writeFieldValue(testInstance, given, method))
        );
    }

    private void writeFieldValue(Object testInstance, Given given, Method method) {
        final Type fieldType = fieldType(given.value(), testInstance);
        final String fileName = fileName(given, testInstance.getClass(), method);
        Object givenObject;
        try {
            final String json = readFromFile(fileName);
            givenObject = jsonConverter.fromJson(json, fieldType);
            log.info("'{}' value was read from '{}'", given.value(), fileName);
        } catch (IOException e) {
            givenObject = randomDataGenerator.generateObject(fieldType);
            writeToFile(jsonConverter.toJson(givenObject, new JsonPropertyFilter()), fileName)
                    .map(File::getAbsolutePath)
                    .ifPresent(file ->
                            log.warn(e.getMessage() + "\nRandom '{}' value file was generated.", given.value())
                    );
        }
        writeField(testInstance, given.value(), givenObject);
    }

    private Type fieldType(String fieldName, Object testInstance) {
        return findObjectField(testInstance, fieldName)
                .map(ObjectField::field)
                .map(Field::getGenericType)
                .orElseThrow(() -> new IllegalArgumentException(format("Test field '{}' not found", fieldName)));
    }

    private String fileName(Given given, Class<?> testClass, Method method) {
        final String fileNamePattern = Optional.of(given.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::givenFileNamePattern);
        return Files.fileName(fileNamePattern, testClass, method, given.value());
    }

    private Stream<Given> findGivenAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Givens.class, true, true))
                .map(Givens::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Given.class).map(Stream::of).orElseGet(Stream::empty));
    }

}
