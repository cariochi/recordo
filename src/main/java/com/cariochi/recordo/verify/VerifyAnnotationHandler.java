package com.cariochi.recordo.verify;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Verifies;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import com.cariochi.recordo.utils.Exceptions;
import com.cariochi.recordo.utils.ExceptionsCollector;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;

public class VerifyAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    @Override
    public void beforeTest(Object testInstance, Method method) {
        Fields.of(testInstance).withTypeAndAnnotation(Expected.class, Verify.class).forEach(
                field -> createExpectedField(field.getAnnotation(Verify.class), testInstance, field)
        );
        findVerifyAnnotations(method).forEach(
                verify -> createExpectedField(verify, testInstance, Fields.of(testInstance).get(verify.field()))
        );
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        final ExceptionsCollector ec = Exceptions.collectorOf(RecordoError.class);
        findVerifyAnnotations(method).forEach(
                ec.consumer(
                        verify -> verifyTestResult(verify, testInstance)
                ));
        if (ec.hasExceptions()) {
            throw new RecordoError(ec.getMessage());
        }
    }

    private void createExpectedField(Verify verify, Object testInstance, TargetField field) {
        if (!(Expected.class.isAssignableFrom(field.getType()))) {
            return;
        }
        final Expected<Object> expected = Expected.builder()
                .annotation(verify)
                .testInstance(testInstance)
                .build();
        field.setValue(expected);
    }

    private void verifyTestResult(Verify verify, Object testInstance) {
        final TargetField field = Fields.of(testInstance).get(verify.field());
        if (Expected.class.isAssignableFrom(field.getType())) {
            return;
        }
        final Object actual = field.getValue();
        if (actual == null) {
            throw new AssertionError(format("Actual '{}' value should not be null", verify.field()));
        }
        field.setValue(null);
        Expected.builder()
                .annotation(verify)
                .testInstance(testInstance)
                .build()
                .assertEquals(actual);
    }

    private Stream<Verify> findVerifyAnnotations(Method method) {
        return findAnnotation(method, Verifies.class)
                .map(Verifies::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Verify.class).map(Stream::of).orElseGet(Stream::empty));
    }
}
