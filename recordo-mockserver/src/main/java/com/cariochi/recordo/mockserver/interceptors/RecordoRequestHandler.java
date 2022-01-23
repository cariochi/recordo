package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;

import java.util.Optional;

public interface RecordoRequestHandler {

    Optional<MockResponse> onRequest(MockRequest request);

    MockResponse onResponse(MockRequest request, MockResponse response);
}
