package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class OkhttpInterceptor implements okhttp3.Interceptor, RecordoInterceptor {

    private final OkhttpMapper mapper = new OkhttpMapper();
    private RecordoRequestHandler handler;

    @Override
    public void setHandler(RecordoRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (handler == null) {
            return chain.proceed(chain.request());
        }
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
