package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.GivenValue;
import com.cariochi.recordo.annotation.Givens;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.ExceptionsSuppressor;
import com.cariochi.recordo.utils.Fields;
import com.cariochi.recordo.utils.Fields.ObjectField;
import com.cariochi.recordo.utils.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Fields.getField;
import static com.cariochi.recordo.utils.Files.*;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;
import static org.slf4j.LoggerFactory.getLogger;

public class GivenAnnotationHandler implements BeforeTestHandler {

    private static final Logger log = getLogger(GivenAnnotationHandler.class);

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    private JsonConverter jsonConverter;

    @Override
    public void beforeTest(Object testInstance, Method method) {
        jsonConverter = JsonConverters.find(testInstance);
        ExceptionsSuppressor.of(RecordoError.class).executeAll(
                Fields.getFieldsWithAnnotation(testInstance, GivenValue.class).stream()
                        .map(field -> () -> setField(field, method))
        );
        ExceptionsSuppressor.of(RecordoError.class).executeAll(
                findGivenAnnotations(method)
                        .map(given -> () -> {
                            final ObjectField field = getField(testInstance, given.value());
                            setField(field, method, given.file());
                        })
        );
    }

    public void setField(ObjectField field, Method method) {
        final GivenValue givenValue = field.getAnnotation(GivenValue.class);
        final String fileNamePattern = Optional.of(givenValue.value())
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::givenValueFileNamePattern);
        final String fileName = Properties.fileName(fileNamePattern, field, method);
        setField(field, fileName);
    }

    private void setField(ObjectField field, Method method, String file) {
        final String fileNamePattern = Optional.of(file)
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::givenFileNamePattern);
        final String fileName = Properties.fileName(fileNamePattern, field, method);
        setField(field, fileName);
    }

    private void setField(ObjectField field, String fileName) {
        Object givenObject;
        try {
            final String json = readFromFile(fileName);
            givenObject = String.class.equals(field.getFieldType())
                    ? json
                    : jsonConverter.fromJson(json, field.getFieldType());
            log.info("Read given '{}' value.\n\t* {}", field.getName(), filePath(fileName));
        } catch (IOException e) {
            givenObject = randomDataGenerator.generateObject(field.getFieldType());
            writeToFile(jsonConverter.toJson(givenObject), fileName)
                    .map(File::getAbsolutePath)
                    .ifPresent(file ->
                            log.warn(e.getMessage() + "\nRandom '{}' value file was generated.", field.getName())
                    );
        }
        field.setValue(givenObject);
    }

    private Stream<Given> findGivenAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Givens.class, true, true))
                .map(Givens::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Given.class).map(Stream::of).orElseGet(Stream::empty));
    }

}
