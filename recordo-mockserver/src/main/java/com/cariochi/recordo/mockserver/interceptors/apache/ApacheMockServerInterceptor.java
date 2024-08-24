package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockResponse;
import org.apache.hc.client5.http.classic.HttpClient;

import java.util.Optional;

import static com.cariochi.recordo.mockserver.interceptors.apache.ApacheClientAttachUtils.*;

public class ApacheMockServerInterceptor implements MockServerInterceptor {

    private final HttpClient httpClient;
    private final OnRequestExecChain onRequestExecChain;
    private final OnResponseExecChain onResponseExecChain;

    public ApacheMockServerInterceptor(HttpClient httpClient) {
        this.httpClient = httpClient;
        onRequestExecChain = attachOnRequestExecChain(httpClient);
        onResponseExecChain = attachOnResponseExecChain(httpClient);
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        onRequestExecChain.setOnRequest(request -> {
            final Optional<MockResponse> response = handler.onRequest(request);
            onResponseExecChain.setActive(response.isEmpty());
            return response;
        });
        onResponseExecChain.setOnResponse(handler::onResponse);
    }

    @Override
    public void close() {
        detachExecChain(httpClient, onRequestExecChain);
        detachExecChain(httpClient, onResponseExecChain);
    }
}
