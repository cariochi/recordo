package com.cariochi.recordo.handler;

import com.cariochi.recordo.given.GivenAnnotationHandler;
import com.cariochi.recordo.httpmock.HttpMockAnnotationHandler;
import com.cariochi.recordo.verify.VerifyAnnotationHandler;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

public class CompositeAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    private final List<AnnotationHandler> handlers;

    public CompositeAnnotationHandler() {
        this.handlers = asList(
                new GivenAnnotationHandler(),
                new HttpMockAnnotationHandler(),
                new VerifyAnnotationHandler()
        );
    }

    public void beforeTest(Object testInstance, Method method) {
        handlers.stream()
                .filter(i -> BeforeTestHandler.class.isAssignableFrom(i.getClass()))
                .map(BeforeTestHandler.class::cast)
                .forEach(processor -> processor.beforeTest(testInstance, method));
    }

    public void afterTest(Object testInstance, Method method) {
        handlers.stream()
                .filter(i -> AfterTestHandler.class.isAssignableFrom(i.getClass()))
                .map(AfterTestHandler.class::cast)
                .forEach(processor -> processor.afterTest(testInstance, method));
    }

}
