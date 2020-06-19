package com.cariochi.recordo.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

public class RecordoContext {

    private final Object testInstance;
    private final Method testMethod;
    private final ParameterContext parameterContext;

    public RecordoContext(ParameterContext parameterContext, ExtensionContext extensionContext) {
        this.parameterContext = parameterContext;
        this.testInstance = extensionContext.getTestInstance().orElse(null);
        this.testMethod = extensionContext.getTestMethod().orElse(null);
    }

    public boolean isAnnotated(Class<? extends Annotation> annotationClass) {
        return parameterContext.isAnnotated(annotationClass);
    }

    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass) {
        return parameterContext.findAnnotation(annotationClass);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return parameterContext.findAnnotation(annotationClass).get();
    }

    public Type getParameterType() {
        return parameterContext.getParameter().getParameterizedType();
    }

    public Class<?> getParameterClass() {
        return parameterContext.getParameter().getType();
    }

    public String getParameterName() {
        return parameterContext.getParameter().getName();
    }

    public Object getTestInstance() {
        return testInstance;
    }

    public Method getTestMethod() {
        return testMethod;
    }
}
