package com.cariochi.recordo.mockhttp.server;

import com.cariochi.recordo.WithMockHttpServer;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockhttp.server.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockhttp.server.interceptors.HttpClientInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Slf4j
public class HttpMocksAnnotationHandler implements BeforeEachCallback, AfterEachCallback {

    private MockHttpServer mockHttpServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), WithMockHttpServer.class)
                .map(WithMockHttpServer::value)
                .ifPresent(file -> {
                    final Object testInstance = context.getRequiredTestInstance();
                    final JsonConverter jsonConverter = JsonConverters.find(testInstance);
                    final HttpClientInterceptor interceptor = HttpClientInterceptors.of(testInstance);
                    mockHttpServer = new MockHttpServer(file, interceptor, jsonConverter);
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), WithMockHttpServer.class)
                .ifPresent(a -> mockHttpServer.close());
    }

}
