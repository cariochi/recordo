package com.cariochi.recordo.interceptor;

import com.cariochi.recordo.Given;
import com.cariochi.recordo.Givens;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.RecordoProperties;
import com.cariochi.recordo.utils.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.cariochi.recordo.utils.ReflectionUtils.findAnnotation;
import static com.cariochi.recordo.utils.ReflectionUtils.writeField;
import static java.lang.String.format;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

@Slf4j
@AllArgsConstructor
public class GivenInterceptor implements BeforeTestInterceptor {

    private final JsonConverter jsonConverter;

    private final Files files = new Files();

    @Override
    public void beforeTest(Object testInstance, Method method) {
        findGivenAnnotations(method).forEach(given -> writeFieldValue(testInstance, given, method));
    }

    @SneakyThrows
    private void writeFieldValue(Object testInstance, Given given, Method method) {
        final Type fieldType = ReflectionUtils.getFieldAndTargetObject(testInstance, given.value())
                .map(Pair::getLeft)
                .map(Field::getGenericType)
                .orElseThrow(() -> new IllegalArgumentException(format("Test field %s not found", given.value())));

        final String fileNamePattern = Optional.of(given.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(RecordoProperties::givenFileNamePattern);

        final String fileName = files.fileName(fileNamePattern, method, given.value());

        final String json = files.readFromFile(fileName);
        final Object o = jsonConverter.fromJson(json, fieldType);
        writeField(testInstance, given.value(), o);

        log.info("`{}` value was read from `{}`", given.value(), fileName);
    }

    private List<Given> findGivenAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Givens.class, true, true))
                .map(Givens::value)
                .map(Arrays::asList)
                .orElseGet(() -> findAnnotation(method, Given.class));
    }
}
