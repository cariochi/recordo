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
import java.util.function.BiFunction;

import static com.cariochi.recordo.httpmock.http.apache.ApacheMapper.*;

public class RecordExecChain implements ClientExecChain {

    private final ClientExecChain requestExecutor;
    private BiFunction<RecordoRequest, RecordoResponse, RecordoResponse> onAfterRequest;
    private boolean active;

    public RecordExecChain(ClientExecChain requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    public void init(BiFunction<RecordoRequest, RecordoResponse, RecordoResponse> onAfterRequest) {
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
            final RecordoRequest recordoRequest = toRecordoRequest(request);
            final RecordoResponse recordoResponse = toRecordoResponse(response);
            return fromRecordoResponse(onAfterRequest.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }
}
