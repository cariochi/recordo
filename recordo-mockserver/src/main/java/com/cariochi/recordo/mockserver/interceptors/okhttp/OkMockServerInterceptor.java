package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.cariochi.reflecto.Reflecto.reflect;

public class OkMockServerInterceptor implements Interceptor, MockServerInterceptor {

    private final OkHttpMapper mapper = new OkHttpMapper();
    private final OkHttpClient httpclient;

    private RecordoRequestHandler handler;

    public OkMockServerInterceptor(OkHttpClient httpClient) {
        this.httpclient = httpClient;
        final List<Interceptor> interceptors = new ArrayList<>(httpClient.interceptors());
        interceptors.add(this);
        reflect(httpClient).fields().get("interceptors").setValue(interceptors);
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        final okhttp3.Request request = chain.request();
        final MockRequest recordoRequest = mapper.toRecordoRequest(request);
        final MockResponse response = handler.onRequest(recordoRequest)
                .orElseGet(() -> handler.onResponse(recordoRequest, proceed(request, chain)));
        return mapper.toOkHttpResponse(request, response);
    }

    @SneakyThrows
    private MockResponse proceed(Request request, Chain chain) {
        final Response response = chain.proceed(request);
        return mapper.toRecordoResponse(response);
    }

    @Override
    public void close() {
        httpclient.interceptors().remove(this);
    }
}
