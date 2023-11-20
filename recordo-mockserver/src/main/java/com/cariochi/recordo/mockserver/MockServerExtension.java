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
import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerExtension implements Extension, BeforeEachCallback, AfterEachCallback {

    private final Map<String, List<RecordoMockServer>> mockServers = new HashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {

        findAnnotation(context.getRequiredTestMethod(), MockServers.class)
                .ifPresent(annotations -> {
                    clear();
                    Stream.of(annotations.value())
                            .forEach(annotation -> createMockServer(annotation, context));
                });

        findAnnotation(context.getRequiredTestMethod(), MockServer.class)
                .ifPresent(annotation -> {
                    clear();
                    createMockServer(annotation, context);

                });
    }

    public MockServerInterceptor interceptor(String clientName, Map<String, ? extends MockServerInterceptor> interceptors) {
        if (interceptors.size() == 1) {
            return interceptors.values().iterator().next();
        }
        if (interceptors.isEmpty()) {
            throw new IllegalArgumentException("No http clients found");
        } else {
            return Optional.ofNullable(interceptors.get(clientName))
                    .orElseThrow(() -> new IllegalArgumentException(format("Multiple http clients found: %s", join(", ", interceptors.keySet()))));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServers.class).ifPresent(a -> clear());
        findAnnotation(context.getRequiredTestMethod(), MockServer.class).ifPresent(a -> clear());
    }

    private void createMockServer(MockServer annotation, ExtensionContext context) {
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), context);
        final JSONCompareMode compareMode = compareMode(annotation.jsonCompareMode().extensible(), annotation.jsonCompareMode().strictOrder());
        final RecordoMockServer mockServer = new RecordoMockServer(annotation.urlPattern(), annotation.value(), jsonConverter, compareMode);
        mockServers.computeIfAbsent(annotation.httpClient(), key -> new ArrayList<>()).add(mockServer);
        final MockServerInterceptor interceptor = HttpClientInterceptors.findInterceptor(annotation.httpClient(), context)
                .orElseThrow(() -> new IllegalArgumentException(
                        isEmpty(annotation.httpClient()) ? "No httpClients found" : format("No bean named '%s' available", annotation.httpClient())
                ));
        interceptor.init(new RoutingRequestHandler(annotation.httpClient()));
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
