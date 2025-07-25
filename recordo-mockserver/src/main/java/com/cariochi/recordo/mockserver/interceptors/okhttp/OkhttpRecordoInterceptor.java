package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.io.IOException;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;

public class OkhttpRecordoInterceptor implements okhttp3.Interceptor, RecordoInterceptor {

    private final OkhttpMapper mapper = new OkhttpMapper();
    private RecordoRequestHandler handler;

    @Override
    public void init(RecordoRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
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

}
