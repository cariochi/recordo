package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.httpmock.model.RecordoRequest;
import com.cariochi.recordo.httpmock.model.RecordoResponse;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface HttpClientInterceptor {

    void init(
            Function<RecordoRequest, Optional<RecordoResponse>> onBeforeRequest,
            BiFunction<RecordoRequest, RecordoResponse, RecordoResponse> onAfterRequest
    );

}
