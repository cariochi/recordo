package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptors;
import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    private final ThreadLocal<List<RecordoMockServer>> mockServers = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServers.class)
                .ifPresent(annotation -> {
                    final JsonConverter jsonConverter = getJsonConverter(context);
                    final MockServerInterceptor interceptor = HttpClientInterceptors.of(context);
                    final List<RecordoMockServer> servers = Stream.of(annotation.value())
                            .map(a -> {
                                final JSONCompareMode compareMode = compareMode(a.jsonCompareMode().extensible(), a.jsonCompareMode().strictOrder());
                                return new RecordoMockServer(a.urlPattern(), a.value(), jsonConverter, compareMode);
                            })
                            .collect(toList());
                    interceptor.init(new RoutingRequestHandler());
                    mockServers.set(servers);
                });
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(annotation -> {
                    final JsonConverter jsonConverter = getJsonConverter(context);
                    final MockServerInterceptor interceptor = HttpClientInterceptors.of(context);
                    final JSONCompareMode compareMode = compareMode(annotation.jsonCompareMode().extensible(), annotation.jsonCompareMode().strictOrder());
                    final RecordoMockServer mockServer = new RecordoMockServer(annotation.urlPattern(), annotation.value(), jsonConverter, compareMode);
                    interceptor.init(new RoutingRequestHandler());
                    mockServers.set(List.of(mockServer));
                });
    }

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServers.class)
                .ifPresent(a -> {
                    mockServers.get().forEach(RecordoMockServer::close);
                    mockServers.remove();
                });
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(a -> {
                    mockServers.get().forEach(RecordoMockServer::close);
                    mockServers.remove();
                });
    }

    private class RoutingRequestHandler implements RecordoRequestHandler {

        @Override
        public Optional<MockResponse> onRequest(MockRequest request) {
            return findServer(request).onRequest(request);
        }

        @Override
        public MockResponse onResponse(MockRequest request, MockResponse response) {
            return findServer(request).onResponse(request, response);
        }

        private RecordoRequestHandler findServer(MockRequest request) {
            return mockServers.get().stream()
                    .filter(server -> server.match(request))
                    .findFirst()
                    .orElseThrow();
        }

    }

}
