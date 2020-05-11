package com.recordo.interceptor;

import com.recordo.json.JsonConverter;
import com.recordo.utils.Files;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

@RequiredArgsConstructor
public abstract class AbstractInterceptor {

    protected final JsonConverter jsonConverter;
    protected final Files files;

    public void beforeTest(Object testInstance, Method method) {}

    public void afterTest(Object testInstance, Method method) {}

    protected <A extends Annotation> List<A> findAnnotation(Method method, Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(method, annotationClass, true, true))
                .map(Arrays::asList)
                .orElse(emptyList());
    }

    protected String fileName(Method method, String propertyName) {
        final String testSuite = replace(uncapitalize(method.getDeclaringClass().getName()), ".", "/");
        final String testName = method.getName();
        return format("%s/%s/%s.json", testSuite, testName, propertyName);
    }
}
