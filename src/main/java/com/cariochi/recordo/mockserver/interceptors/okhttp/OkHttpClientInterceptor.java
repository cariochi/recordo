package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockHttpRequest;
import com.cariochi.recordo.mockserver.model.MockHttpResponse;
import com.cariochi.recordo.utils.reflection.Fields;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class OkHttpClientInterceptor implements Interceptor, HttpClientInterceptor {

    private final OkHttpMapper mapper = new OkHttpMapper();

    private RecordoRequestHandler handler;

    public static OkHttpClientInterceptor attachTo(OkHttpClient httpClient) {
        final OkHttpClientInterceptor interceptor = new OkHttpClientInterceptor();
        final List<Interceptor> interceptors = httpClient.interceptors().stream()
                .filter(interceptor1 -> !(interceptor1 instanceof OkHttpClientInterceptor))
                .collect(toList());
        interceptors.add(interceptor);
        Fields.of(httpClient).get("interceptors").setValue(interceptors);
        return interceptor;
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final MockHttpRequest recordoRequest = mapper.toRecordoRequest(request);
        final MockHttpResponse response = handler.onRequest(recordoRequest)
                .orElseGet(() -> handler.onResponse(recordoRequest, proceed(request, chain)));
        return mapper.toOkHttpResponse(request, response);
    }

    @SneakyThrows
    private MockHttpResponse proceed(Request request, Chain chain) {
        final Response response = chain.proceed(request);
        return mapper.toRecordoResponse(response);
    }
}
