package com.cariochi.recordo.mockhttp.server;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockhttp.server.interceptors.HttpClientInterceptors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockHttpServer {

    private final String fileName;
    private final Object testInstance;

    public MockHttpContext run() {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final MockHttpContext mockHttpContext = new MockHttpContext(fileName, jsonConverter);
        HttpClientInterceptors.of(testInstance).init(mockHttpContext::pollMock, mockHttpContext::addMock);
        return mockHttpContext;
    }
}
