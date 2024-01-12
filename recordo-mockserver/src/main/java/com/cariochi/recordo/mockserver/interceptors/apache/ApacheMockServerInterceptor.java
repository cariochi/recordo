package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.util.Optional;
import org.apache.http.client.HttpClient;

public class ApacheMockServerInterceptor implements MockServerInterceptor {

    private final OnRequestExecChain onRequestExecChain;
    private final OnResponseExecChain onResponseExecChain;

    public static ApacheMockServerInterceptor attachTo(HttpClient httpClient) {
        return new ApacheMockServerInterceptor(httpClient);
    }

    private ApacheMockServerInterceptor(HttpClient httpClient) {
        onRequestExecChain = ApacheClientAttachUtils.attachOnRequestExecChain(httpClient);
        onResponseExecChain = ApacheClientAttachUtils.attachOnResponseExecChain(httpClient);
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        onRequestExecChain.onRequest(request -> {
            final Optional<MockResponse> response = handler.onRequest(request);
            onResponseExecChain.setActive(response.isEmpty());
            return response;
        });
        onResponseExecChain.onResponse(handler::onResponse);
    }

}
