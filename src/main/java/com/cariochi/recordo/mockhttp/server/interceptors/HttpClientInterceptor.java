package com.cariochi.recordo.mockhttp.server.interceptors;

import com.cariochi.recordo.mockhttp.server.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.server.model.MockHttpResponse;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface HttpClientInterceptor {

    void init(
            Function<MockHttpRequest, Optional<MockHttpResponse>> onBeforeRequest,
            BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest
    );

}
