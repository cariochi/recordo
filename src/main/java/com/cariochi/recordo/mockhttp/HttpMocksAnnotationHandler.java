package com.cariochi.recordo.mockhttp;

import com.cariochi.recordo.MockHttp;
import com.cariochi.recordo.mockhttp.interceptors.HttpClientInterceptors;
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
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), MockHttp.class)
                .map(MockHttp::value)
                .ifPresent(file -> {
                    final Object testInstance = context.getRequiredTestInstance();
                    mockHttpContext = new MockHttpServer(file, testInstance).run();
                    HttpClientInterceptors.of(testInstance).init(mockHttpContext::pollMock, mockHttpContext::addMock);
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), MockHttp.class)
                .ifPresent(a -> mockHttpContext.close());
    }

}
