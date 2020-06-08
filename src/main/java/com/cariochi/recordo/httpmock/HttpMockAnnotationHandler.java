package com.cariochi.recordo.httpmock;

import com.cariochi.recordo.annotation.HttpMock;
import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.httpmock.assertion.RequestAssert;
import com.cariochi.recordo.httpmock.http.HttpClientInterceptors;
import com.cariochi.recordo.httpmock.model.RecordoHttpMock;
import com.cariochi.recordo.httpmock.model.RecordoRequest;
import com.cariochi.recordo.httpmock.model.RecordoResponse;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.Files;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.cariochi.recordo.utils.Properties.*;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpMockAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    private static final Logger log = getLogger(HttpMockAnnotationHandler.class);
    private static final Type TYPE = new TypeReference<List<RecordoHttpMock>>() {}.getType();

    private JsonConverter jsonConverter;

    private List<RecordoHttpMock> expectedMocks;
    private final List<RecordoHttpMock> actualMocks = new ArrayList<>();
    private final Files files = new Files();

    @Override
    public void beforeTest(Object testInstance, Method method) {
        final Optional<HttpMock> annotation = findAnnotation(method, HttpMock.class);
        if (!annotation.isPresent()) {
            return;
        }
        jsonConverter = JsonConverters.find(testInstance);
        loadExpectedMocks(testInstance.getClass(), method);
        actualMocks.clear();
        HttpClientInterceptors.of(testInstance).init(this::playback, this::record);
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        final Optional<HttpMock> annotation = findAnnotation(method, HttpMock.class);
        if (!annotation.isPresent()) {
            return;
        }
        if (!actualMocks.isEmpty()) {
            actualMocks.forEach(this::prepareForRecord);
            final String json = jsonConverter.toJson(actualMocks);
            final String fileName = fileName(httpMocksFileNamePattern(), testInstance.getClass(), method.getName(), "");
            files.writeToFile(json, fileName)
                    .ifPresent(file -> log.info(
                            "Record Http Mocks\n\t* {}\n{}",
                            file.getAbsolutePath(), urlsOf(actualMocks)
                    ));
        }
    }

    public Optional<RecordoResponse> playback(RecordoRequest request) {
        if (expectedMocks.isEmpty()) {
            return Optional.empty();
        }
        final RecordoHttpMock expected = expectedMocks.remove(0);
        log.info("Playback Http Mock\n\t\t- {}", request.getMethod() + " " + request.getUrl());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        RequestAssert.assertEquals(expected.getRequest(), request);
        return Optional.of(expected.getResponse());
    }

    public RecordoResponse record(RecordoRequest request, RecordoResponse response) {
        actualMocks.add(new RecordoHttpMock(request, response));
        return response;
    }

    private void loadExpectedMocks(Class<?> testClass, Method method) {
        try {
            final String fileName = fileName(httpMocksFileNamePattern(), testClass, method.getName(), "");
            expectedMocks = jsonConverter.fromJson(files.readFromFile(fileName), TYPE);
            expectedMocks.forEach(this::prepareForPlayback);
            log.info("Read Http Mocks.\n\t* {}\n{}", files.filePath(fileName), urlsOf(expectedMocks));
        } catch (IOException e) {
            log.warn("{}", e.getMessage());
            expectedMocks = emptyList();
        }
    }

    private void prepareForRecord(RecordoHttpMock mock) {
        final RecordoRequest request = mock.getRequest();
        request.setHeaders(filteredHeaders(request.getHeaders()));
        Optional.ofNullable(request.getBody())
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(toObject())
                .ifPresent(request::setBody);

        final RecordoResponse response = mock.getResponse();
        response.setHeaders(filteredHeaders(response.getHeaders()));
        Optional.ofNullable(response.getBody())
                .filter(body -> body instanceof String)
                .map(String.class::cast)
                .map(toObject())
                .ifPresent(response::setBody);
    }

    private Map<String, String> filteredHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(header -> httpMocksIncludedHeaders().contains(header.getKey().toLowerCase()))
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> httpMocksSensitiveHeaders().contains(entry.getKey().toLowerCase())
                                ? "********"
                                : entry.getValue()
                ));
    }

    private void prepareForPlayback(RecordoHttpMock mock) {
        final RecordoRequest request = mock.getRequest();
        Optional.ofNullable(request.getBody())
                .filter(body -> !(body instanceof String))
                .map(toJson())
                .ifPresent(request::setBody);

        final RecordoResponse response = mock.getResponse();
        Optional.ofNullable(response.getBody())
                .filter(body -> !(body instanceof String))
                .map(toJson())
                .ifPresent(response::setBody);
    }

    private String urlsOf(List<RecordoHttpMock> mocks) {
        return mocks.stream()
                .map(RecordoHttpMock::getRequest)
                .map(req -> "\t\t- " + req.getMethod() + " " + req.getUrl())
                .collect(joining("\n"));
    }

    private Function<Object, String> toJson() {
        return body -> jsonConverter.toJson(body);
    }

    private Function<String, Object> toObject() {
        return json -> jsonConverter.fromJson(json, Object.class);
    }
}
