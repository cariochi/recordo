package com.cariochi.recordo.restmocks.http;

import com.cariochi.recordo.restmocks.model.RequestMock;
import com.cariochi.recordo.restmocks.model.ResponseMock;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface HttpClientInterceptor {

    void init(
            Function<RequestMock, Optional<ResponseMock>> onBeforeRequest,
            BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest
    );

}
