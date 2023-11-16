package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptors;
import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    private final Map<String, List<RecordoMockServer>> mockServers = new HashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {

        final Map<String, ? extends MockServerInterceptor> interceptors = HttpClientInterceptors.findAll(context);

        findAnnotation(context.getRequiredTestMethod(), MockServers.class)
                .ifPresent(annotations -> {
                    clear();
                    Stream.of(annotations.value())
                            .forEach(annotation -> {
                                final JsonConverter jsonConverter = getJsonConverter(context);
                                final JSONCompareMode compareMode = compareMode(annotation.jsonCompareMode().extensible(), annotation.jsonCompareMode().strictOrder());
                                final RecordoMockServer mockServer = new RecordoMockServer(annotation.urlPattern(), annotation.value(), jsonConverter, compareMode);
                                interceptor(annotation.name(), interceptors).init(new RoutingRequestHandler(annotation.name()));
                                final List<RecordoMockServer> servers = mockServers.computeIfAbsent(annotation.name(), key -> new ArrayList<>());
                                servers.add(mockServer);
                            });
                });

        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(annotation -> {
                    clear();
                    final JsonConverter jsonConverter = getJsonConverter(context);
                    final JSONCompareMode compareMode = compareMode(annotation.jsonCompareMode().extensible(), annotation.jsonCompareMode().strictOrder());
                    final RecordoMockServer mockServer = new RecordoMockServer(annotation.urlPattern(), annotation.value(), jsonConverter, compareMode);
                    interceptor(annotation.name(), interceptors).init(mockServer);
                    final List<RecordoMockServer> servers = mockServers.computeIfAbsent(annotation.name(), key -> new ArrayList<>());
                    servers.add(mockServer);
                });
    }


    public MockServerInterceptor interceptor(String name, Map<String, ? extends MockServerInterceptor> all) {
        if (all.isEmpty()) {
            throw new IllegalArgumentException("Http Client not found");
        } else if ("DEFAULT".equals(name) && all.size() == 1) {
            return all.values().stream().findFirst().orElseThrow();
        } else {
            return Optional.ofNullable(all.get(name))
                    .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServers.class)
                .ifPresent(a -> clear());
        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(a -> clear());
    }

    private void clear() {
        mockServers.values().stream().flatMap(Collection::stream).forEach(RecordoMockServer::close);
        mockServers.clear();
    }

    @RequiredArgsConstructor
    private class RoutingRequestHandler implements RecordoRequestHandler {

        private final String name;

        @Override
        public Optional<MockResponse> onRequest(MockRequest request) {
            return findServer(request).onRequest(request);
        }

        @Override
        public MockResponse onResponse(MockRequest request, MockResponse response) {
            return findServer(request).onResponse(request, response);
        }

        private RecordoRequestHandler findServer(MockRequest request) {
            return mockServers.get(name).stream()
                    .filter(server -> server.match(request))
                    .findFirst()
                    .orElseThrow();
        }

    }

}
