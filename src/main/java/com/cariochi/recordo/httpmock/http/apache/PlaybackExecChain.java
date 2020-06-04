package com.cariochi.recordo.httpmock.http.apache;

import com.cariochi.recordo.httpmock.model.RecordoRequest;
import com.cariochi.recordo.httpmock.model.RecordoResponse;
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


public class PlaybackExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private final ApacheMapper mapper = new ApacheMapper();

    private Function<RecordoRequest, Optional<RecordoResponse>> onBeforeRequest;

    public PlaybackExecChain(ClientExecChain requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    public void init(Function<RecordoRequest, Optional<RecordoResponse>> onBeforeRequest) {
        this.onBeforeRequest = onBeforeRequest;
    }

    @Override
    public CloseableHttpResponse execute(HttpRoute route,
                                         HttpRequestWrapper request,
                                         HttpClientContext context,
                                         HttpExecutionAware executionAware) throws IOException, HttpException {
        final Optional<RecordoResponse> recordoResponse = onBeforeRequest.apply(mapper.toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? mapper.fromRecordoResponse(recordoResponse.get())
                : requestExecutor.execute(route, request, context, executionAware);
    }
}
