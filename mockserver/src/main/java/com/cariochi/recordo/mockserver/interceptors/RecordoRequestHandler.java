package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.mockserver.model.MockHttpRequest;
import com.cariochi.recordo.mockserver.model.MockHttpResponse;

import java.util.Optional;

public interface RecordoRequestHandler {

    Optional<MockHttpResponse> onRequest(MockHttpRequest request);

    MockHttpResponse onResponse(MockHttpRequest request, MockHttpResponse response);
}
