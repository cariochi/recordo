package com.cariochi.recordo.httpmock.http.apache;

import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;
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

    private BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest;
    private boolean active;

    public void init(BiFunction<RequestMock, ResponseMock, ResponseMock> onAfterRequest) {
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
            final RequestMock recordoRequest = mapper.toRecordoRequest(request);
            final ResponseMock recordoResponse = mapper.toRecordoResponse(response);
            return mapper.fromRecordoResponse(onAfterRequest.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }
}
