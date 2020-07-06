package com.cariochi.recordo.mockhttp.interceptors.okhttp;

import com.cariochi.recordo.mockhttp.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockhttp.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.model.MockHttpResponse;
import com.cariochi.recordo.utils.reflection.Fields;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cariochi.recordo.utils.exceptions.Exceptions.tryGet;
import static java.util.stream.Collectors.toList;

public class OkHttpClientInterceptor implements Interceptor, HttpClientInterceptor {

    private final OkHttpMapper mapper = new OkHttpMapper();

    private Function<MockHttpRequest, Optional<MockHttpResponse>> onBeforeRequest;
    private BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest;

    public OkHttpClientInterceptor(OkHttpClient httpClient) {
        attachToHttpClient(httpClient);
    }

    @Override
    public void init(Function<MockHttpRequest, Optional<MockHttpResponse>> onBeforeRequest,
                     BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest) {
        this.onBeforeRequest = onBeforeRequest;
        this.onAfterRequest = onAfterRequest;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final MockHttpRequest recordoRequest = mapper.toRecordoRequest(request);
        final MockHttpResponse response = onBeforeRequest.apply(recordoRequest)
                .orElseGet(tryGet(
                        () -> onAfterRequest.apply(recordoRequest, mapper.toRecordoResponse(chain.proceed(request)))
                ));
        return mapper.fromRecordoResponse(request, response);
    }

    private void attachToHttpClient(OkHttpClient httpClient) {
        final List<Interceptor> interceptors = httpClient.interceptors().stream()
                .filter(interceptor -> !(interceptor instanceof OkHttpClientInterceptor))
                .collect(toList());
        interceptors.add(this);
        Fields.of(httpClient).get("interceptors").setValue(interceptors);
    }

}
