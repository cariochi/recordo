package com.cariochi.recordo.httpmock;

import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.httpmock.assertion.RequestAssert;
import com.cariochi.recordo.httpmock.http.HttpClientInterceptors;
import com.cariochi.recordo.httpmock.model.HttpMock;
import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.Files;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.cariochi.recordo.utils.Properties.*;
import static com.cariochi.recordo.utils.Reflection.findAnnotation;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

@Slf4j
public class HttpMockAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    private static final Type TYPE = new TypeReference<List<HttpMock>>() {}.getType();

    private JsonConverter jsonConverter;

    private List<HttpMock> expectedMocks;
    private final List<HttpMock> actualMocks = new ArrayList<>();
    private final Files files = new Files();

    @Override
    public void beforeTest(Object testInstance, Method method) {
        final Optional<com.cariochi.recordo.annotation.HttpMock> annotation = findAnnotation(method,
                com.cariochi.recordo.annotation.HttpMock.class);
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
        final Optional<com.cariochi.recordo.annotation.HttpMock> annotation = findAnnotation(method,
                com.cariochi.recordo.annotation.HttpMock.class);
        if (!annotation.isPresent()) {
            return;
        }
        if (!actualMocks.isEmpty()) {
            final List<HttpMock> mocksToRecord = actualMocks.stream()
                    .map(this::prepareForRecord)
                    .collect(toList());
            final String json = jsonConverter.toJson(mocksToRecord);
            final String fileName = fileName(httpMocksFileNamePattern(), testInstance.getClass(), method.getName(), "");
            files.writeToFile(json, fileName)
                    .ifPresent(file -> log.info(
                            "Http mocks are recorded.\n\t* {}\n{}",
                            file.getAbsolutePath(),
                            urlsOf(actualMocks)
                    ));
        }
    }

    public Optional<ResponseMock> playback(RequestMock request) {
        if (expectedMocks.isEmpty()) {
            return Optional.empty();
        }
        final HttpMock expected = expectedMocks.remove(0);
        log.info("Playback Http Mock\n\t\t- {}", request.getMethod() + " " + request.getUrl());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        RequestAssert.assertEquals(expected.getRequest(), request);
        return Optional.of(expected.getResponse());
    }

    public ResponseMock record(RequestMock request, ResponseMock response) {
        actualMocks.add(new HttpMock(request, response));
        return response;
    }

    private void loadExpectedMocks(Class<?> testClass, Method method) {
        try {
            final String fileName = fileName(httpMocksFileNamePattern(), testClass, method.getName(), "");
            final List<HttpMock> mocks = jsonConverter.fromJson(files.readFromFile(fileName), TYPE);
            expectedMocks = mocks.stream()
                    .map(this::prepareForPlayback)
                    .collect(toList());
            log.info("Read Http Mocks.\n\t* {}\n{}", files.filePath(fileName), urlsOf(expectedMocks));
        } catch (FileNotFoundException e) {
            log.warn("{}", e.getMessage());
            expectedMocks = emptyList();
        }
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

    private HttpMock prepareForRecord(HttpMock mock) {
        final RequestMock request = Optional.ofNullable(mock.getRequest().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(mock.getRequest()::withBody)
                .orElse(mock.getRequest().withBody(null));
        final ResponseMock response = Optional.ofNullable(mock.getResponse().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new HttpMock(request, response);
    }

    private HttpMock prepareForPlayback(HttpMock mock) {
        final RequestMock request = Optional.ofNullable(mock.getRequest().getBody())
                .map(body -> jsonConverter.toJson(body))
                .map(mock.getRequest()::withBody)
                .orElse(mock.getRequest().withBody(null));
        final ResponseMock response = Optional.ofNullable(mock.getResponse().getBody())
                .map(body -> jsonConverter.toJson(body))
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new HttpMock(request, response);
    }

    private String urlsOf(List<HttpMock> mocks) {
        return mocks.stream()
                .map(HttpMock::getRequest)
                .map(req -> "\t\t- " + req.getMethod() + " " + req.getUrl())
                .collect(joining("\n"));
    }

}
