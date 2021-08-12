package com.cariochi.recordo.mockhttp.server.interceptors;

import com.cariochi.recordo.mockhttp.server.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.server.model.MockHttpResponse;

import java.util.Optional;

public interface RecordoRequestHandler {

    Optional<MockHttpResponse> onRequest(MockHttpRequest request);

    MockHttpResponse onResponse(MockHttpRequest request, MockHttpResponse response);
}
