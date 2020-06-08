package com.cariochi.recordo.verify;

import com.cariochi.recordo.annotation.Verifies;
import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import com.cariochi.recordo.utils.ExceptionsSuppressor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;

public class VerifyAnnotationHandler implements AfterTestHandler {

    private final Verifier verifier = new Verifier();

    @Override
    public void afterTest(Object testInstance, Method method) {
        ExceptionsSuppressor.of(AssertionError.class).executeAll(
                findVerifyAnnotations(method).map(verify -> () -> verifyTestResult(verify, method, testInstance))
        );
    }

    private void verifyTestResult(Verify verify, Method method, Object testInstance) {
        final TargetField field = Fields.of(testInstance).get(verify.value());
        final Object actual = field.getValue();
        if (actual == null) {
            throw new AssertionError(format("Actual '{}' value should not be null", verify.value()));
        }
        field.setValue(null);
        verifier.verify(actual, verify, testInstance, method, field.getName());
    }

    private Stream<Verify> findVerifyAnnotations(Method method) {
        return findAnnotation(method, Verifies.class)
                .map(Verifies::value)
                .map(Arrays::stream)
                .orElseGet(() -> findAnnotation(method, Verify.class).map(Stream::of).orElseGet(Stream::empty));
    }
}
