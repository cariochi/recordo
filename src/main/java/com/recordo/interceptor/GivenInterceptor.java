package com.recordo.interceptor;

import com.recordo.Given;
import com.recordo.Givens;
import com.recordo.json.JsonConverter;
import com.recordo.utils.Files;
import com.recordo.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.recordo.utils.ReflectionUtils.writeField;
import static java.lang.String.format;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

public class GivenInterceptor extends AbstractInterceptor {

    public GivenInterceptor(String rootFolder, JsonConverter jsonConverter) {
        super(jsonConverter, new Files(rootFolder));
    }

    @Override
    public void beforeTest(Object testInstance, Method method) {
        findGivenAnnotations(method).forEach(given -> writeFieldValue(testInstance, given, method));
    }

    private void writeFieldValue(Object testInstance, Given given, Method method) {
        final Type fieldType = ReflectionUtils.getFieldAndTargetObject(testInstance, given.value())
                .map(Pair::getLeft)
                .map(Field::getGenericType)
                .orElseThrow(() -> new IllegalArgumentException(format("Test field %s not found", given.value())));

        final String fileName = Optional.of(given.file())
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> fileName(method, given.value()));

        files.readFromFile(fileName)
                .map(json -> jsonConverter.fromJson(json, fieldType))
                .ifPresent(o -> writeField(testInstance, given.value(), o));
    }

    private List<Given> findGivenAnnotations(Method method) {
        return Optional.ofNullable(getAnnotation(method, Givens.class, true, true))
                .map(Givens::value)
                .map(Arrays::asList)
                .orElseGet(() -> findAnnotation(method, Given.class));
    }
}
