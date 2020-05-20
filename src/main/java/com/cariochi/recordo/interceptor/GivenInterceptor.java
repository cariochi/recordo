package com.cariochi.recordo.interceptor;

import com.cariochi.recordo.Given;
import com.cariochi.recordo.Givens;
import com.cariochi.recordo.RecordoException;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.cariochi.recordo.utils.ExceptionsSuppressor;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import static com.cariochi.recordo.utils.ReflectionUtils.*;
import static java.lang.String.format;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;
import static org.slf4j.LoggerFactory.getLogger;

public class GivenInterceptor implements BeforeTestInterceptor {

    private static final Logger log = getLogger(GivenInterceptor.class);

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    private final JsonConverter jsonConverter;

    public GivenInterceptor(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void beforeTest(Object testInstance, Method method) {
        ExceptionsSuppressor.of(RecordoException.class).executeAll(
                findGivenAnnotations(method).map(given -> () -> writeFieldValue(testInstance, given, method))
        );
    }

    private void writeFieldValue(Object testInstance, Given given, Method method) {
        final Type fieldType = fieldType(given.value(), testInstance);
        final String fileName = fileName(given, method);
        try {
            final Object givenObject = jsonConverter.fromJson(readFromFile(fileName), fieldType);
            writeField(testInstance, given.value(), givenObject);
            log.info("`{}` value was read from `{}`", given.value(), fileName);
        } catch (IOException e) {
            final String message = generateFile(fieldType, fileName)
                    .map(file -> format("\nRandom '%s' value file was generated.", given.value()))
                    .orElse("");
            throw new RecordoException(e.getMessage() + message);
        }
    }

    private Type fieldType(String fieldName, Object testInstance) {
        return getFieldAndTargetObject(testInstance, fieldName)
                .map(Pair::getLeft)
                .map(Field::getGenericType)
                .orElseThrow(() -> new IllegalArgumentException(format("Test field %s not found", fieldName)));
    }

    private String fileName(Given given, Method method) {
        final String fileNamePattern = Optional.of(given.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(Properties::givenFileNamePattern);
        return Files.fileName(fileNamePattern, method, given.value());
    }

    private Optional<File> generateFile(Type fieldType, String fileName) {
        return Optional.ofNullable(randomDataGenerator.generateObject(fieldType))
                .flatMap(o -> writeToFile(jsonConverter.toJson(o, new JsonPropertyFilter()), fileName));
    }

    private Stream<Given> findGivenAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Givens.class, true, true))
                .map(Givens::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Given.class));
    }

}
