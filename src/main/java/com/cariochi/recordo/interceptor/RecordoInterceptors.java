package com.cariochi.recordo.interceptor;

import com.cariochi.recordo.json.JsonConverter;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

public class RecordoInterceptors {

    private final List<AbstractInterceptor> processors;

    public RecordoInterceptors(String rootFolder, JsonConverter jsonConverter) {
        processors = asList(
                new VerifyInterceptor(rootFolder, jsonConverter),
                new GivenInterceptor(rootFolder, jsonConverter)
        );
    }

    public void beforeTest(Object testInstance, Method method) {
        processors.forEach(processor -> processor.beforeTest(testInstance, method));
    }

    public void afterTest(Object testInstance, Method method) {
        processors.forEach(processor -> processor.afterTest(testInstance, method));
    }

}
