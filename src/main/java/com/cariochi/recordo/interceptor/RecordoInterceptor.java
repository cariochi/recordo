package com.cariochi.recordo.interceptor;

import lombok.Builder;
import lombok.Singular;

import java.lang.reflect.Method;
import java.util.List;

@Builder
public class RecordoInterceptor implements BeforeTestInterceptor, AfterTestInterceptor {

    @Singular
    private final List<Interceptor> interceptors;

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
