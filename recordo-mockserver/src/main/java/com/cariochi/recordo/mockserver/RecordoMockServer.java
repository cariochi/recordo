package com.cariochi.recordo.mockserver;


import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.Files;
import com.cariochi.recordo.core.utils.Properties;
import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockInteraction;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static com.cariochi.recordo.core.utils.Files.isJson;
import static com.cariochi.reflecto.types.Types.listOf;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.text.StringSubstitutor.replace;
import static org.skyscreamer.jsonassert.JSONCompare.compareJSON;

@Slf4j
public class RecordoMockServer implements AutoCloseable, RecordoRequestHandler {

    private final UrlPatternMatcher urlPatternMatcher;
    private final String mocksPath;
    private final JSONCompareMode compareMode;
    private final JsonConverter jsonConverter;
    private final List<MockInteraction> actualMocks = new ArrayList<>();
    private List<MockInteraction> expectedMocks;
    private final Map<String, Object> variables = new HashMap<>();

    public RecordoMockServer(RecordoInterceptor interceptor, String mocksPath) {
        this("**", mocksPath, new JsonConverter(), compareMode(false, true));
        interceptor.init(this);
    }

    public RecordoMockServer(String urlPattern, String mocksPath, JsonConverter jsonConverter, JSONCompareMode compareMode) {
        this.urlPatternMatcher = new UrlPatternMatcher(urlPattern);
        this.mocksPath = mocksPath;
        this.jsonConverter = jsonConverter;
        this.compareMode = compareMode;
    }

    @SneakyThrows
    public boolean match(MockRequest request) {
        if (!urlPatternMatcher.match(request.getUrl())) {
            return false;
        }
        if (expectedMocks().isEmpty()) {
            return true;
        }
        if (expectedMocks().size() < actualMocks.size()) {
            return false;
        }
        try {
            response(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    @Override
    public Optional<MockResponse> onRequest(MockRequest request) {
        if (expectedMocks().isEmpty()) {
            return Optional.empty();
        }
        log.info("Playback Http Mock: [{}] {}", request.getMethod(), request.getUrl());
        final MockResponse response = response(request);
        final MockInteraction actualInteraction = new MockInteraction(request, response);
        actualMocks.add(actualInteraction);
        return Optional.of(prepareForPlayback(response));
    }

    @SneakyThrows
    private MockResponse response(MockRequest request) {
        final MockInteraction expectedMock = expectedMocks().get(actualMocks.size());
        request.setHeaders(filteredHeaders(request.getHeaders()));
        final MockRequest actualRequest = prepareForRecord(request);

        final String expectedJson = jsonConverter.toJson(expectedMock.getRequest());
        final String actualJson = jsonConverter.toJson(actualRequest);
        final JSONCompareResult compareResult = compareJSON(expectedJson, actualJson, compareMode);
        if (compareResult.failed()) {
            reportFailureToConsole(actualRequest);
            throw new AssertionError(compareResult.getMessage() + "\n" + "Expected Request:\n" + expectedJson + "\n" + "Actual Request:\n" + actualJson);
        }
        return expectedMock.getResponse();
    }

    private void reportFailureToConsole(MockRequest actualRequest) {
        final MockInteraction actualInteraction = new MockInteraction(actualRequest, new MockResponse());
        final List<MockInteraction> mocksToRecord = Stream.concat(actualMocks.stream(), Stream.of(actualInteraction))
                .map(this::prepareForRecord)
                .toList();

        final Path path = Path.of(mocksPath);
        final Path actualPath = isJson(mocksPath)
                ? Path.of(path.getParent().toString(), "ACTUAL", path.getFileName().toString())
                : Path.of(path.toString(), "ACTUAL");

        saveActualMocks(mocksToRecord, actualPath.toString());
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
        if (expectedMocks().isEmpty()) {

            final List<MockInteraction> mocksToRecord = actualMocks.stream()
                    .map(this::prepareForRecord)
                    .toList();

            saveActualMocks(mocksToRecord, mocksPath);

        } else if (actualMocks.size() < expectedMocks().size()) {
            expectedMocks = null;
            throw new AssertionError("Not all mocks requests were called");
        }
    }

    private void saveActualMocks(List<MockInteraction> mocks, String path) {
        if (isJson(path)) {
            final String json = jsonConverter.toJson(mocks);
            Files.write(json, path)
                    .ifPresent(file -> log.info("Actual http mocks are saved to file://{}:\n{}", file, urlsOf(actualMocks)));
        } else {
            for (int i = 0; i < mocks.size(); i++) {
                MockInteraction mock = mocks.get(i);
                final String json = jsonConverter.toJson(mock);
                final URI uri = URI.create(mock.getRequest().getUrl());
                final String url = StringUtils.replace(uri.getHost() + "_" + uri.getPath(), "/", "_");
                final String filename = format("%03d__%s__%s.json", (i + 1), mock.getRequest().getMethod(), url);
                Files.write(json, Path.of(path, filename).toString())
                        .ifPresent(file -> log.info("Actual http mock is saved to file://{}:\n{}", file, urlsOf(actualMocks)));
            }
        }
    }

    private List<MockInteraction> expectedMocks() {
        if (expectedMocks == null) {
            expectedMocks = Stream.of(mocksPath)
                    .map(this::loadExpectedMocks)
                    .flatMap(List::stream)
                    .toList();
        }
        return expectedMocks;
    }

    @SneakyThrows
    private List<MockInteraction> loadExpectedMocks(String path) {
        if (Files.exists(path)) {
            if (isJson(path)) {
                final String json = applyVariables(Files.readString(path));
                final List<MockInteraction> mocks = jsonConverter.fromJson(json, listOf(MockInteraction.class));
                log.info("Read Http Mocks from file://{}\nRequests:\n{}", Files.path(path), urlsOf(mocks));
                return mocks;
            } else {
                final List<Path> fileNames = Files.getFileList(path);
                return fileNames.stream()
                        .map(Path::toString)
                        .filter(Files::isJson)
                        .map(file -> {
                            final String json = applyVariables(Files.readString(file));
                            final MockInteraction mock = jsonConverter.fromJson(json, MockInteraction.class);
                            log.info("Read Http Mock from file://{}\nRequest:\n{}", Files.path(file), mock.getRequest().getUrl());
                            return mock;
                        })
                        .toList();
            }
        } else {
            final String migratedFromPath = isJson(path)
                    ? StringUtils.removeEnd(path, ".json")
                    : StringUtils.removeEnd(path, "/") + ".json";
            if (Files.exists(migratedFromPath)) {
                final List<MockInteraction> mockInteractions = loadExpectedMocks(migratedFromPath);
                saveActualMocks(mockInteractions, path);
                Files.delete(migratedFromPath);
                return mockInteractions;
            }
            log.warn("File {} not found", path);
            return emptyList();
        }
    }

    private String applyVariables(String string) {
        return Optional.of(string)
                .map(s -> replace(s, variables.entrySet().stream()
                        .filter(e -> e.getValue() instanceof CharSequence)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)))
                )
                .map(s -> replace(s, variables, "\"${", "}\""))
                .map(s -> replace(s, variables)).orElse(string);
    }

    private MockInteraction prepareForRecord(MockInteraction mock) {
        return new MockInteraction(prepareForRecord(mock.getRequest()), prepareForRecord(mock.getResponse()));
    }

    private MockRequest prepareForRecord(MockRequest request) {
        final MockRequest prepared = Optional.ofNullable(request)
                .filter(MockRequest::isJson)
                .map(MockRequest::getBody)
                .filter(String.class::isInstance)
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
                .filter(String.class::isInstance)
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
                        entry -> Properties.httpMocksSensitiveHeaders().contains(entry.getKey().toLowerCase()) ? "********" : entry.getValue()
                ));
    }

    private String urlsOf(List<MockInteraction> mocks) {
        return mocks.stream()
                .map(MockInteraction::getRequest)
                .map(req -> format("-[%s] %s", req.getMethod(), req.getUrl()))
                .collect(joining("\n"));
    }

}
