package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.execchain.ClientExecChain;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class OnRequestExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final ApacheMapper mapper = new ApacheMapper();

    private Function<MockRequest, Optional<MockResponse>> onRequest;

    public void onRequest(Function<MockRequest, Optional<MockResponse>> onRequest) {
        this.onRequest = onRequest;
    }

    @Override
    public CloseableHttpResponse execute(HttpRoute route,
                                         HttpRequestWrapper request,
                                         HttpClientContext context,
                                         HttpExecutionAware executionAware) throws IOException, HttpException {
        final Optional<MockResponse> recordoResponse = onRequest.apply(mapper.toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? mapper.toHttpResponse(recordoResponse.get())
                : requestExecutor.execute(route, request, context, executionAware);
    }
}
