package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.mockserver.assertion.RequestAssert;
import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockHttpInteraction;
import com.cariochi.recordo.mockserver.model.MockHttpRequest;
import com.cariochi.recordo.mockserver.model.MockHttpResponse;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.Properties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.text.StringSubstitutor.replace;

@Slf4j
public class RecordoMockServer implements AutoCloseable, RecordoRequestHandler {

    private static final Type TYPE = new TypeReference<List<MockHttpInteraction>>() {}.getType();

    private final String fileName;

    private JsonConverter jsonConverter = new JacksonConverter();

    private final List<MockHttpInteraction> actualMocks = new ArrayList<>();
    private List<MockHttpInteraction> expectedMocks;

    private final Map<String, Object> variables = new HashMap<>();

    private int index = 0;

    public RecordoMockServer(String fileName, HttpClientInterceptor interceptor) {
        this.fileName = fileName;
        interceptor.init(this);
    }

    public RecordoMockServer(String fileName, HttpClientInterceptor interceptor, JsonConverter jsonConverter) {
        this(fileName, interceptor);
        this.jsonConverter = jsonConverter;
    }

    @Override
    public Optional<MockHttpResponse> onRequest(MockHttpRequest request) {
        if (expectedMocks().isEmpty()) {
            return Optional.empty();
        }
        final MockHttpInteraction expected = prepareForPlayback(expectedMocks().get(index++));
        log.info("Playback Http Mock: [{}] {}", request.getMethod(), request.getUrl());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        RequestAssert.assertEquals(expected.getRequest(), request);
        return Optional.of(expected.getResponse());
    }

    @Override
    public MockHttpResponse onResponse(MockHttpRequest request, MockHttpResponse response) {
        actualMocks.add(new MockHttpInteraction(request, response));
        return response;
    }

    public void set(String name, Object value) {
        variables.put(name, value);
        expectedMocks = null;
    }

    @Override
    public void close() {
        if (!actualMocks.isEmpty()) {
            final List<MockHttpInteraction> mocksToRecord = actualMocks.stream()
                    .map(this::prepareForRecord)
                    .collect(toList());
            final String json = jsonConverter.toJson(mocksToRecord);

            Files.write(json, fileName)
                    .ifPresent(file -> log.info("Http mocks are recorded to file://{}:\n{}", file, urlsOf(actualMocks)));
        }
    }

    private List<MockHttpInteraction> expectedMocks() {
        if (expectedMocks == null) {
            loadExpectedMocks();
        }
        return expectedMocks;
    }

    private void loadExpectedMocks() {
        try {
            final String json = applyVariables(Files.read(fileName));
            expectedMocks = jsonConverter.fromJson(json, TYPE);
            log.info("Read Http Mocks from file {}\nRequests:\n{}", Files.path(fileName), urlsOf(expectedMocks));
        } catch (NoSuchFileException e) {
            log.warn("{}", e.getMessage());
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

    private MockHttpInteraction prepareForRecord(MockHttpInteraction mock) {
        return new MockHttpInteraction(
                prepareForRecord(mock.getRequest()),
                prepareForRecord(mock.getResponse())
        );
    }

    private MockHttpRequest prepareForRecord(MockHttpRequest request) {
        final MockHttpRequest prepared = Optional.ofNullable(request)
                .filter(MockHttpRequest::isJson)
                .map(MockHttpRequest::getBody)
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(json -> jsonConverter.fromJson(json, Object.class))
                .map(request::withBody)
                .orElse(request);
        return prepared.withHeaders(filteredHeaders(request.getHeaders()));
    }

    private MockHttpResponse prepareForRecord(MockHttpResponse request) {
        final MockHttpResponse prepared = Optional.ofNullable(request)
                .filter(MockHttpResponse::isJson)
                .map(MockHttpResponse::getBody)
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(json -> jsonConverter.fromJson(json, Object.class))
                .map(request::withBody)
                .orElse(request);
        return prepared.withHeaders(filteredHeaders(request.getHeaders()));
    }

    private MockHttpInteraction prepareForPlayback(MockHttpInteraction mock) {
        return new MockHttpInteraction(
                prepareForPlayback(mock.getRequest()),
                prepareForPlayback(mock.getResponse())
        );
    }

    private MockHttpRequest prepareForPlayback(MockHttpRequest request) {
        return Optional.ofNullable(request)
                .filter(MockHttpRequest::isJson)
                .map(MockHttpRequest::getBody)
                .filter(body -> !(body instanceof String))
                .map(jsonConverter::toJson)
                .map(request::withBody)
                .orElse(request);
    }

    private MockHttpResponse prepareForPlayback(MockHttpResponse response) {
        return Optional.ofNullable(response)
                .filter(MockHttpResponse::isJson)
                .map(MockHttpResponse::getBody)
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

    private String urlsOf(List<MockHttpInteraction> mocks) {
        return mocks.stream()
                .map(MockHttpInteraction::getRequest)
                .map(req -> format("-[{}] {}", req.getMethod(), req.getUrl()))
                .collect(joining("\n"));
    }

}
