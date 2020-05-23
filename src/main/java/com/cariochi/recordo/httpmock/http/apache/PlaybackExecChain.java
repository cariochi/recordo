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

import static com.cariochi.recordo.httpmock.http.apache.ApacheMapper.fromRecordoResponse;
import static com.cariochi.recordo.httpmock.http.apache.ApacheMapper.toRecordoRequest;

public class PlaybackExecChain implements ClientExecChain {

    private Function<RecordoRequest, Optional<RecordoResponse>> onBeforeRequest;
    private final ClientExecChain requestExecutor;

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
        final Optional<RecordoResponse> recordoResponse = onBeforeRequest.apply(toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? fromRecordoResponse(recordoResponse.get())
                : requestExecutor.execute(route, request, context, executionAware);
    }
}
