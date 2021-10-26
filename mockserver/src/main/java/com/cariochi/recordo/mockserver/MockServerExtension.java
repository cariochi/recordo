package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonUtils;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerExtension implements BeforeEachCallback, AfterEachCallback {

    private final ThreadLocal<RecordoMockServer> mockServer = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(annotation -> {
                    final Object testInstance = context.getRequiredTestInstance();
                    final JsonConverter jsonConverter = getJsonConverter(testInstance);
                    final HttpClientInterceptor interceptor = HttpClientInterceptors.of(testInstance);
                    final JSONCompareMode compareMode = JsonUtils.compareMode(annotation.extensible(), annotation.strictOrder());
                    mockServer.set(new RecordoMockServer(annotation.value(), interceptor, jsonConverter, compareMode));
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(a -> {
                    mockServer.get().close();
                    mockServer.remove();
                });
    }

}
