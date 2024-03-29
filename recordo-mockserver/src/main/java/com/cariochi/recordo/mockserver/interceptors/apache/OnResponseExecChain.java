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
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class OnResponseExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final ApacheMapper mapper = new ApacheMapper();

    private BiFunction<MockRequest, MockResponse, MockResponse> onResponse;
    private boolean active;

    public void onResponse(BiFunction<MockRequest, MockResponse, MockResponse> onResponse) {
        this.onResponse = onResponse;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public CloseableHttpResponse execute(HttpRoute route,
                                         HttpRequestWrapper request,
                                         HttpClientContext context,
                                         HttpExecutionAware executionAware) throws IOException, HttpException {
        final CloseableHttpResponse response = requestExecutor.execute(route, request, context, executionAware);
        if (active) {
            final MockRequest recordoRequest = mapper.toRecordoRequest(request);
            final MockResponse recordoResponse = mapper.toRecordoResponse(response);
            return mapper.toHttpResponse(onResponse.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }
}
