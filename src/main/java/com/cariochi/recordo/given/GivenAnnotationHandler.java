package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.annotation.Givens;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import com.cariochi.recordo.utils.Exceptions;
import com.cariochi.recordo.utils.ExceptionsCollector;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Reflection.findAnnotation;

public class GivenAnnotationHandler implements BeforeTestHandler {

    @Override
    public void beforeTest(Object testInstance, Method method) {
        final ExceptionsCollector ec = Exceptions.collectorOf(RecordoError.class);
        Fields.of(testInstance).withAnnotation(Given.class).forEach(
                ec.consumer(
                        field -> processGiven(field.getAnnotation(Given.class), field)
                ));
        findGivenAnnotations(method).forEach(
                ec.consumer(
                        given -> processGiven(given, Fields.of(testInstance).get(given.field()))
                ));
        if (ec.hasExceptions()) {
            throw new RecordoError(ec.getMessage());
        }
    }

    public void processGiven(Given given, TargetField field) {
        final Object value = GivenObject.builder()
                .testInstance(field.getTarget())
                .file(given.value())
                .parameterType(field.getGenericType())
                .build()
                .get();
        field.setValue(value);
    }

    private Stream<Given> findGivenAnnotations(Method method) {
        return findAnnotation(method, Givens.class)
                .map(Givens::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Given.class).map(Stream::of).orElseGet(Stream::empty));
    }

}
