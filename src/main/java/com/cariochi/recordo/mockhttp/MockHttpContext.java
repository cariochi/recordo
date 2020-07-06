package com.cariochi.recordo.mockhttp;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.mockhttp.assertion.RequestAssert;
import com.cariochi.recordo.mockhttp.model.MockHttpInteraction;
import com.cariochi.recordo.mockhttp.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.model.MockHttpResponse;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.utils.Properties;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.text.StringSubstitutor.replace;

@Slf4j
@RequiredArgsConstructor
public class MockHttpContext implements AutoCloseable {

    private static final Type TYPE = new TypeReference<List<MockHttpInteraction>>() {}.getType();

    private final String fileName;
    private final JsonConverter jsonConverter;

    private final List<MockHttpInteraction> actualMocks = new ArrayList<>();
    private List<MockHttpInteraction> expectedMocks;

    private final Map<String, Object> variables = new HashMap<>();

    private int index = 0;

    Optional<MockHttpResponse> pollMock(MockHttpRequest request) {
        if (expectedMocks().isEmpty()) {
            return Optional.empty();
        }
        final MockHttpInteraction expected = prepareForPlayback(expectedMocks().get(index++));
        log.info("Playback Http Mock: [{}] {}", request.getMethod(), request.getUrl());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        RequestAssert.assertEquals(expected.getRequest(), request);
        return Optional.of(expected.getResponse());
    }

    MockHttpResponse addMock(MockHttpRequest request, MockHttpResponse response) {
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
                    .ifPresent(file -> log.info("Http mocks are recorded to file {}:\n{}", file, urlsOf(actualMocks)));
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
        final MockHttpRequest request = Optional.ofNullable(mock.getRequest().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(body -> mock.getRequest().withBody(body))
                .orElse(mock.getRequest().withBody(null));
        final MockHttpResponse response = Optional.ofNullable(mock.getResponse().getBody())
                .map(json -> jsonConverter.fromJson((String) json, Object.class))
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new MockHttpInteraction(
                request.withHeaders(filteredHeaders(request.getHeaders())),
                response.withHeaders(filteredHeaders(response.getHeaders()))
        );
    }

    private MockHttpInteraction prepareForPlayback(MockHttpInteraction mock) {
        final MockHttpRequest request = Optional.ofNullable(mock.getRequest().getBody())
                .map(jsonConverter::toJson)
                .map(mock.getRequest()::withBody)
                .orElse(mock.getRequest().withBody(null));
        final MockHttpResponse response = Optional.ofNullable(mock.getResponse().getBody())
                .map(jsonConverter::toJson)
                .map(mock.getResponse()::withBody)
                .orElse(mock.getResponse().withBody(null));
        return new MockHttpInteraction(request, response);
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
