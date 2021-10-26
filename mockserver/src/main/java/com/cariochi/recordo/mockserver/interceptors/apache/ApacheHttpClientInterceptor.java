package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockHttpResponse;
import org.apache.http.client.HttpClient;

import java.util.Optional;

public class ApacheHttpClientInterceptor implements HttpClientInterceptor {

    private final OnRequestExecChain onRequestExecChain;
    private final OnResponseExecChain onResponseExecChain;

    public ApacheHttpClientInterceptor(HttpClient httpClient) {
        onRequestExecChain = ApacheClientAttachUtils.attachOnRequestExecChain(httpClient);
        onResponseExecChain = ApacheClientAttachUtils.attachOnResponseExecChain(httpClient);
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        onRequestExecChain.onRequest(request -> {
            final Optional<MockHttpResponse> response = handler.onRequest(request);
            onResponseExecChain.setActive(!response.isPresent());
            return response;
        });
        onResponseExecChain.onResponse(handler::onResponse);
    }
}
