package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.MockServer;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerAnnotationHandler implements BeforeEachCallback, AfterEachCallback {

    private RecordoMockServer mockServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .map(MockServer::value)
                .ifPresent(file -> {
                    final Object testInstance = context.getRequiredTestInstance();
                    final JsonConverter jsonConverter = JsonConverters.find(testInstance);
                    final HttpClientInterceptor interceptor = HttpClientInterceptors.of(testInstance);
                    mockServer = new RecordoMockServer(file, interceptor, jsonConverter);
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(a -> mockServer.close());
    }

}
