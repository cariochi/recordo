package com.cariochi.recordo.restmocks;

import com.cariochi.recordo.annotation.RestMocks;
import com.cariochi.recordo.handler.AfterTestHandler;
import com.cariochi.recordo.handler.BeforeTestHandler;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.restmocks.assertion.RequestAssert;
import com.cariochi.recordo.restmocks.http.HttpClientInterceptors;
import com.cariochi.recordo.restmocks.model.RequestMock;
import com.cariochi.recordo.restmocks.model.ResponseMock;
import com.cariochi.recordo.restmocks.model.RestMock;
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
public class RestMocksAnnotationHandler implements BeforeTestHandler, AfterTestHandler {

    private static final Type TYPE = new TypeReference<List<RestMock>>() {}.getType();

    private JsonConverter jsonConverter;
    private List<RestMock> expectedMocks;
    private final List<RestMock> actualMocks = new ArrayList<>();

    @Override
    public void beforeTest(Object testInstance, Method method) {
        final Optional<RestMocks> annotation = findAnnotation(method, RestMocks.class);
        if (!annotation.isPresent()) {
            return;
        }
        jsonConverter = JsonConverters.find(testInstance);
        final String fileName = composeFileName(annotation.get().value(), testInstance.getClass());
        loadExpectedMocks(fileName);
        actualMocks.clear();
        HttpClientInterceptors.of(testInstance).init(this::playback, this::record);
    }

    @Override
    public void afterTest(Object testInstance, Method method) {
        final Optional<RestMocks> annotation = findAnnotation(method, RestMocks.class);
        if (!annotation.isPresent()) {
            return;
        }
        if (!actualMocks.isEmpty()) {
            final List<RestMock> mocksToRecord = actualMocks.stream()
                    .map(this::prepareForRecord)
                    .collect(toList());
            final String json = jsonConverter.toJson(mocksToRecord);

            final String fileName = composeFileName(annotation.get().value(), testInstance.getClass());
            Files.writeToFile(json, fileName)
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
        final RestMock expected = expectedMocks.remove(0);
        log.info("Playback Http Mock\n\t\t- {}", request.getMethod() + " " + request.getUrl());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        RequestAssert.assertEquals(expected.getRequest(), request);
        return Optional.of(expected.getResponse());
    }

    public ResponseMock record(RequestMock request, ResponseMock response) {
        actualMocks.add(new RestMock(request, response));
        return response;
    }

    private void loadExpectedMocks(String fileName) {
        try {
            final List<RestMock> mocks = jsonConverter.fromJson(Files.readFromFile(fileName), TYPE);
            expectedMocks = mocks.stream().map(this::prepareForPlayback).collect(toList());
            log.info("Read Http Mocks.\n\t* {}\n{}", Files.filePath(fileName), urlsOf(expectedMocks));
        } catch (FileNotFoundException e) {
            log.warn("{}", e.getMessage());
            expectedMocks = emptyList();
        }
    }

    private Map<String, String> filteredHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(header -> httpMocksIncludedHeaders().contains(header.getKey().toLowerCase()))
                .collect(toMap(
                        stringStringEntry -> stringStringEntry.getKey().toLowerCase(),
                        entry -> httpMocksSensitiveHeaders().contains(entry.getKey().toLowerCase())
                                ? "********"
                                : entry.getValue()
                ));
    }

    private RestMock prepareForRecord(RestMock mock) {
        final RequestMock request = Optional.ofNullable(mock.getRequest().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(body -> mock.getRequest().withBody(body))
                .orElse(mock.getRequest().withBody(null));
        final ResponseMock response = Optional.ofNullable(mock.getResponse().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new RestMock(
                request.withHeaders(filteredHeaders(request.getHeaders())),
                response.withHeaders(filteredHeaders(response.getHeaders()))
        );
    }

    private RestMock prepareForPlayback(RestMock mock) {
        final RequestMock request = Optional.ofNullable(mock.getRequest().getBody())
                .map(body -> jsonConverter.toJson(body))
                .map(mock.getRequest()::withBody)
                .orElse(mock.getRequest().withBody(null));
        final ResponseMock response = Optional.ofNullable(mock.getResponse().getBody())
                .map(body -> jsonConverter.toJson(body))
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new RestMock(request, response);
    }

    private String urlsOf(List<RestMock> mocks) {
        return mocks.stream()
                .map(RestMock::getRequest)
                .map(req -> "\t\t- " + req.getMethod() + " " + req.getUrl())
                .collect(joining("\n"));
    }

}
