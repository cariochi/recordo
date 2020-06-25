package com.cariochi.recordo.restmocks.http.apache;

import com.cariochi.recordo.restmocks.model.RequestMock;
import com.cariochi.recordo.restmocks.model.ResponseMock;
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
public class PlaybackExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final ApacheMapper mapper = new ApacheMapper();

    private Function<RequestMock, Optional<ResponseMock>> onBeforeRequest;

    public void init(Function<RequestMock, Optional<ResponseMock>> onBeforeRequest) {
        this.onBeforeRequest = onBeforeRequest;
    }

    @Override
    public CloseableHttpResponse execute(HttpRoute route,
                                         HttpRequestWrapper request,
                                         HttpClientContext context,
                                         HttpExecutionAware executionAware) throws IOException, HttpException {
        final Optional<ResponseMock> recordoResponse = onBeforeRequest.apply(mapper.toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? mapper.fromRecordoResponse(recordoResponse.get())
                : requestExecutor.execute(route, request, context, executionAware);
    }
}
