package com.cariochi.recordo.mockhttp.interceptors.apache;

import com.cariochi.recordo.mockhttp.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.model.MockHttpResponse;
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
public class RecordExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final ApacheMapper mapper = new ApacheMapper();

    private BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest;
    private boolean active;

    public void init(BiFunction<MockHttpRequest, MockHttpResponse, MockHttpResponse> onAfterRequest) {
        this.onAfterRequest = onAfterRequest;
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
            return mapper.fromRecordoResponse(onAfterRequest.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }
}
