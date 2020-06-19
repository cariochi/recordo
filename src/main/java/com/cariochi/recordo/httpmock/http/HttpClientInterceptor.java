package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface HttpClientInterceptor {

    void init(
            Function<RequestMock, Optional<ResponseMock>> onBeforeRequest,
            BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest
    );

}
