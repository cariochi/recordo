package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Givens;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import com.cariochi.recordo.utils.ExceptionsSuppressor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Properties.*;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;

public class GivenAnnotationHandler implements BeforeTestHandler {

    private final GivenFileReader givenFileReader = new GivenFileReader();

    @Override
    public void beforeTest(Object testInstance, Method method) {
        ExceptionsSuppressor.of(RecordoError.class).executeAll(
                Fields.of(testInstance).findAll(Given.class).stream()
                        .map(field -> () -> processGivenOnField(method, field, field.getAnnotation(Given.class)))
        );
        ExceptionsSuppressor.of(RecordoError.class).executeAll(
                findGivenAnnotations(method)
                        .map(given -> () -> processGivenOnMethod(testInstance, method, given))
        );
    }

    public void processGivenOnField(Method method, TargetField field, Given given) {
        final String pattern = givenValueFileNamePattern(given.file());
        processGiven(pattern, method, field);
    }

    public void processGivenOnMethod(Object testInstance, Method method, Given given) {
        final TargetField field = Fields.of(testInstance).get(given.value());
        final String pattern = givenFileNamePattern(given.file());
        processGiven(pattern, method, field);
    }

    public void processGiven(String pattern, Method method, TargetField field) {
        final String fileName = fileName(pattern, field.getTargetClass(), method.getName(), field.getName());
        field.setValue(givenFileReader.readFromFile(
                fileName,
                field.getGenericType(),
                field.getName(),
                JsonConverters.find(field.getTarget())
        ));
    }

    private Stream<Given> findGivenAnnotations(Method method) {
        return findAnnotation(method, Givens.class)
                .map(Givens::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Given.class).map(Stream::of).orElseGet(Stream::empty));
    }

}
