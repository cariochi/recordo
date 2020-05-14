package com.cariochi.recordo.junit5;

import com.cariochi.recordo.interceptor.GivenInterceptor;
import com.cariochi.recordo.interceptor.RecordoInterceptor;
import com.cariochi.recordo.interceptor.VerifyInterceptor;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RecordoExtension implements BeforeEachCallback, AfterEachCallback {

    private final RecordoInterceptor interceptors;

    public RecordoExtension() {
        this(new JacksonConverter());
    }

    public RecordoExtension(JsonConverter jsonConverter) {
        final VerifyInterceptor verifyInterceptor = new VerifyInterceptor(jsonConverter);
        final GivenInterceptor givenInterceptor = new GivenInterceptor(jsonConverter);
        this.interceptors = RecordoInterceptor.builder()
                .interceptor(verifyInterceptor)
                .interceptor(givenInterceptor)
                .build();
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
