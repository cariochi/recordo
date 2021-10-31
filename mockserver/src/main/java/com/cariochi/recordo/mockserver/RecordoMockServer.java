package com.cariochi.recordo.mockserver;


import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.Files;
import com.cariochi.recordo.core.utils.Properties;
import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockInteraction;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.lang.reflect.Type;
import java.util.*;

import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.text.StringSubstitutor.replace;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

@Slf4j
public class RecordoMockServer implements AutoCloseable, RecordoRequestHandler {

    private static final Type TYPE = new TypeReference<List<MockInteraction>>() {}.getType();

    private final String fileName;
    private final JSONCompareMode compareMode;
    private final JsonConverter jsonConverter;
    private final List<MockInteraction> actualMocks = new ArrayList<>();
    private List<MockInteraction> expectedMocks;
    private final Map<String, Object> variables = new HashMap<>();
    private int index = 0;

    public RecordoMockServer(String fileName, MockServerInterceptor interceptor) {
        this(fileName, interceptor, new JsonConverter(), compareMode(false, true));
    }

    public RecordoMockServer(String fileName,
                             MockServerInterceptor interceptor,
                             JsonConverter jsonConverter,
                             JSONCompareMode compareMode) {
        this.fileName = fileName;
        this.jsonConverter = jsonConverter;
        this.compareMode = compareMode;
        interceptor.init(this);
    }

    @SneakyThrows
    @Override
    public Optional<MockResponse> onRequest(MockRequest request) {
        if (expectedMocks().isEmpty()) {
            return Optional.empty();
        }
        final MockInteraction mock = expectedMocks().get(index++);
        log.info("Playback Http Mock: [{}] {}", request.getMethod(), request.getUrl());

        request.setHeaders(filteredHeaders(request.getHeaders()));

        final String expected = jsonConverter.toJson(mock.getRequest());
        final String actual = jsonConverter.toJson(prepareForRecord(request));
        final JSONCompareResult compareResult = compareJSON(expected, actual, compareMode);

        if (compareResult.failed()) {
            throw new AssertionError(compareResult.getMessage()
                    + "\n" + "Expected Request:\n" + expected
                    + "\n" + "Actual Request:\n" + actual
            );
        }

        return Optional.of(prepareForPlayback(mock.getResponse()));
    }

    @Override
    public MockResponse onResponse(MockRequest request, MockResponse response) {
        actualMocks.add(new MockInteraction(request, response));
        return response;
    }

    public void set(String name, Object value) {
        variables.put(name, value);
        expectedMocks = null;
    }

    @Override
    public void close() {
        if (!actualMocks.isEmpty()) {
            final List<MockInteraction> mocksToRecord = actualMocks.stream()
                    .map(this::prepareForRecord)
                    .collect(toList());

            final String json = jsonConverter.toJson(mocksToRecord);

            Files.write(json, fileName)
                    .ifPresent(file -> log.info("Http mocks are recorded to file://{}:\n{}", file, urlsOf(actualMocks)));
        }
    }

    private List<MockInteraction> expectedMocks() {
        if (expectedMocks == null) {
            loadExpectedMocks();
        }
        return expectedMocks;
    }

    private void loadExpectedMocks() {
        if (Files.exists(fileName)) {
            final String json = applyVariables(Files.read(fileName));
            expectedMocks = jsonConverter.fromJson(json, TYPE);
            log.info("Read Http Mocks from file {}\nRequests:\n{}", Files.path(fileName), urlsOf(expectedMocks));
        } else {
            log.warn("File {} not found", fileName);
            expectedMocks = emptyList();
        }
    }

    private String applyVariables(String string) {
        return Optional.of(string)
                .map(s -> replace(s, variables.entrySet().stream()
                        .filter(e -> e.getValue() instanceof CharSequence)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                ))
                .map(s -> replace(s, variables, "\"${", "}\""))
                .map(s -> replace(s, variables))
                .orElse(string);
    }

    private MockInteraction prepareForRecord(MockInteraction mock) {
        return new MockInteraction(
                prepareForRecord(mock.getRequest()),
                prepareForRecord(mock.getResponse())
        );
    }

    private MockRequest prepareForRecord(MockRequest request) {
        final MockRequest prepared = Optional.ofNullable(request)
                .filter(MockRequest::isJson)
                .map(MockRequest::getBody)
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(json -> jsonConverter.fromJson(json, Object.class))
                .map(request::withBody)
                .orElse(request);
        return prepared.withHeaders(filteredHeaders(request.getHeaders()));
    }

    private MockResponse prepareForRecord(MockResponse request) {
        final MockResponse prepared = Optional.ofNullable(request)
                .filter(MockResponse::isJson)
                .map(MockResponse::getBody)
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(json -> jsonConverter.fromJson(json, Object.class))
                .map(request::withBody)
                .orElse(request);
        return prepared.withHeaders(filteredHeaders(request.getHeaders()));
    }

    private MockResponse prepareForPlayback(MockResponse response) {
        return Optional.ofNullable(response)
                .filter(MockResponse::isJson)
                .map(MockResponse::getBody)
                .filter(body -> !(body instanceof String))
                .map(jsonConverter::toJson)
                .map(response::withBody)
                .orElse(response);
    }

    private Map<String, String> filteredHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(header -> Properties.httpMocksIncludedHeaders().contains(header.getKey().toLowerCase()))
                .collect(toMap(
                        stringStringEntry -> stringStringEntry.getKey().toLowerCase(),
                        entry -> Properties.httpMocksSensitiveHeaders().contains(entry.getKey().toLowerCase())
                                ? "********"
                                : entry.getValue()
                ));
    }

    private String urlsOf(List<MockInteraction> mocks) {
        return mocks.stream()
                .map(MockInteraction::getRequest)
                .map(req -> format("-[%s] %s", req.getMethod(), req.getUrl()))
                .collect(joining("\n"));
    }

}
