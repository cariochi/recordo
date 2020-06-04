package com.cariochi.recordo.junit5;

import com.cariochi.recordo.handler.CompositeAnnotationHandler;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RecordoExtension implements BeforeEachCallback, AfterEachCallback {

    private final CompositeAnnotationHandler interceptor = new CompositeAnnotationHandler();

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.beforeTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.afterTest(testInstance, context.getRequiredTestMethod())
        );
    }

}
