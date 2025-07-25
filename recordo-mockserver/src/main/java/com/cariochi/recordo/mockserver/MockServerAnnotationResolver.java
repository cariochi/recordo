package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.RegularExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static com.cariochi.recordo.mockserver.interceptors.InterceptorInstallers.findInterceptor;
import static java.lang.String.format;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

@Slf4j
public class MockServerAnnotationResolver implements RegularExtension, BeforeEachCallback, AfterEachCallback {

    private final Map<String, List<RecordoMockServer>> mockServers = new HashMap<>();
    private final Set<InterceptorInstaller> installers = new HashSet<>();

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

    @Override
    public void afterEach(ExtensionContext context) {
        findAnnotation(context.getRequiredTestMethod(), MockServers.class).ifPresent(a -> clear());
        findAnnotation(context.getRequiredTestMethod(), MockServer.class).ifPresent(a -> clear());
    }

    private void createMockServer(MockServer annotation, ExtensionContext context) {
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), context);
        final JSONCompareMode compareMode = compareMode(annotation.jsonCompareMode().extensible(), annotation.jsonCompareMode().strictOrder());
        final RecordoMockServer mockServer = new RecordoMockServer(annotation.urlPattern(), annotation.value(), jsonConverter, compareMode);
        mockServers.computeIfAbsent(annotation.beanName(), key -> new ArrayList<>()).add(mockServer);

        final InterceptorInstaller installer = findInterceptor(annotation.beanName(), context)
                .orElseThrow(() -> new IllegalArgumentException(format("No http client bean named '%s' available.", annotation.beanName())));

        installer.init(new RoutingRequestHandler(annotation.beanName()));
        installers.add(installer);
    }

    @SneakyThrows
    private void clear() {
        final List<AssertionError> errors = mockServers.values().stream()
                .flatMap(Collection::stream)
                .map(server -> {
                    try {
                        server.close();
                        return null;
                    } catch (AssertionError e) {
                        return e;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        mockServers.clear();
        installers.forEach(InterceptorInstaller::close);
        installers.clear();
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
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
