package com.cariochi.recordo.mockhttp.server;

import com.cariochi.recordo.MockHttpServer;
import com.cariochi.recordo.mockhttp.server.interceptors.HttpClientInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;

@Slf4j
public class HttpMocksAnnotationHandler implements BeforeEachCallback, AfterEachCallback {

    private MockHttpContext mockHttpContext;

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), MockHttpServer.class)
                .map(MockHttpServer::value)
                .ifPresent(file -> {
                    final Object testInstance = context.getRequiredTestInstance();
                    mockHttpContext = new com.cariochi.recordo.mockhttp.server.MockHttpServer(file, testInstance).run();
                    HttpClientInterceptors.of(testInstance).init(mockHttpContext::pollMock, mockHttpContext::addMock);
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), MockHttpServer.class)
                .ifPresent(a -> mockHttpContext.close());
    }

}
