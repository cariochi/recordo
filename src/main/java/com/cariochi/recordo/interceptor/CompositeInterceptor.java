package com.cariochi.recordo.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

public class CompositeInterceptor implements BeforeTestInterceptor, AfterTestInterceptor {

    private final List<Interceptor> interceptors;

    public CompositeInterceptor(Interceptor... interceptors) {
        this.interceptors = asList(interceptors);
    }

    public void beforeTest(Object testInstance, Method method) {
        interceptors.stream()
                .filter(i -> BeforeTestInterceptor.class.isAssignableFrom(i.getClass()))
                .map(BeforeTestInterceptor.class::cast)
                .forEach(processor -> processor.beforeTest(testInstance, method));
    }

    public void afterTest(Object testInstance, Method method) {
        interceptors.stream()
                .filter(i -> AfterTestInterceptor.class.isAssignableFrom(i.getClass()))
                .map(AfterTestInterceptor.class::cast)
                .forEach(processor -> processor.afterTest(testInstance, method));
    }

}
