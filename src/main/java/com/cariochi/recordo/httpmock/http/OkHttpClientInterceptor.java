package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.httpmock.http.okhttp.OkHttpMapper;
import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;
import com.cariochi.recordo.reflection.Fields;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.cariochi.recordo.utils.Exceptions.trying;
import static java.util.stream.Collectors.toList;

public class OkHttpClientInterceptor implements Interceptor, HttpClientInterceptor {

    private final OkHttpMapper mapper = new OkHttpMapper();

    private Function<RequestMock, Optional<ResponseMock>> onBeforeRequest;
    private BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest;

    public OkHttpClientInterceptor(OkHttpClient httpClient) {
        attachToHttpClient(httpClient);
    }

    @Override
    public void init(Function<RequestMock, Optional<ResponseMock>> onBeforeRequest,
                     BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest) {
        this.onBeforeRequest = onBeforeRequest;
        this.onAfterRequest = onAfterRequest;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final RequestMock recordoRequest = mapper.toRecordoRequest(request);
        final ResponseMock response = onBeforeRequest.apply(recordoRequest)
                .orElseGet(trying(
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
