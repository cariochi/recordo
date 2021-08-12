package com.cariochi.recordo.mockhttp.server.interceptors.apache;

import com.cariochi.recordo.mockhttp.server.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.server.model.MockHttpResponse;
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

    private BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onResponse;
    private boolean active;

    public void onResponse(BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onResponse) {
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
            final MockHttpRequest recordoRequest = mapper.toRecordoRequest(request);
            final MockHttpResponse recordoResponse = mapper.toRecordoResponse(response);
            return mapper.toHttpResponse(onResponse.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }
}
