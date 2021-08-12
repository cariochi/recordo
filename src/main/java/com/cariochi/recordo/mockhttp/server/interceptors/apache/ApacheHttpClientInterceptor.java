package com.cariochi.recordo.mockhttp.server.interceptors.apache;

import com.cariochi.recordo.mockhttp.server.interceptors.HttpClientInterceptor;
import com.cariochi.recordo.mockhttp.server.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockhttp.server.model.MockHttpResponse;
import org.apache.http.client.HttpClient;

import java.util.Optional;

import static com.cariochi.recordo.mockhttp.server.interceptors.apache.ApacheClientAttachUtils.attachOnRequestExecChain;
import static com.cariochi.recordo.mockhttp.server.interceptors.apache.ApacheClientAttachUtils.attachOnResponseExecChain;

public class ApacheHttpClientInterceptor implements HttpClientInterceptor {

    private final OnRequestExecChain onRequestExecChain;
    private final OnResponseExecChain onResponseExecChain;

    public ApacheHttpClientInterceptor(HttpClient httpClient) {
        onRequestExecChain = attachOnRequestExecChain(httpClient);
        onResponseExecChain = attachOnResponseExecChain(httpClient);
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
