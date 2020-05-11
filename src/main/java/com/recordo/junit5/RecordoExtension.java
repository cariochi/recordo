package com.recordo.junit5;

import com.recordo.interceptor.RecordoInterceptors;
import com.recordo.json.JacksonConverter;
import com.recordo.json.JsonConverter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RecordoExtension implements BeforeEachCallback, AfterEachCallback {

    private final RecordoInterceptors interceptors;

    public RecordoExtension() {
        this("", new JacksonConverter());
    }

    public RecordoExtension(String rootFolder, JsonConverter jsonConverter) {
        this.interceptors = new RecordoInterceptors(rootFolder, jsonConverter);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptors.beforeTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptors.afterTest(testInstance, context.getRequiredTestMethod())
        );
    }

}
