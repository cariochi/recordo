package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.annotation.EnableHttpMocks;
import com.cariochi.recordo.httpmock.model.RecordoRequest;
import com.cariochi.recordo.httpmock.model.RecordoResponse;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cariochi.recordo.utils.Reflection.readAnnotatedValue;

public interface HttpClientInterceptor {

    void init(
            Function<RecordoRequest, Optional<RecordoResponse>> onBeforeRequest,
            BiFunction<RecordoRequest, RecordoResponse, RecordoResponse> onAfterRequest
    );

    static HttpClientInterceptor of(Object testInstance) {
        return okHttpClientInterceptor(testInstance)
                .map(Optional::of)
                .orElseGet(() -> apacheHttpClientInterceptor(testInstance))
                .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
    }

    static Optional<HttpClientInterceptor> okHttpClientInterceptor(Object testInstance) {
        return readAnnotatedValue(testInstance, OkHttpClientInterceptor.class, EnableHttpMocks.class)
                .map(Optional::of)
                .orElseGet(() -> okHttpClient(testInstance).map(OkHttpClientInterceptor::new))
                .map(HttpClientInterceptor.class::cast);
    }

    static Optional<OkHttpClient> okHttpClient(Object testInstance) {
        return readAnnotatedValue(testInstance, OkHttpClient.class, EnableHttpMocks.class);
    }

    static Optional<HttpClientInterceptor> apacheHttpClientInterceptor(Object testInstance) {
        return readAnnotatedValue(testInstance, HttpClient.class, EnableHttpMocks.class)
                .map(ApacheHttpClientInterceptor::new);
    }
}
