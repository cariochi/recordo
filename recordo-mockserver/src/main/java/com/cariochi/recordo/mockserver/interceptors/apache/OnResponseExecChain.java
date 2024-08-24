package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.Setter;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.util.function.BiFunction;

public class OnResponseExecChain extends AbstractExecChainHandler {

    @Setter
    private BiFunction<MockRequest, MockResponse, MockResponse> onResponse;

    @Setter
    private boolean active;

    public OnResponseExecChain(ExecChainHandler execChainHandler) {
        super(execChainHandler);
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request,
                                       ExecChain.Scope scope,
                                       ExecChain chain) throws IOException, HttpException {
        ClassicHttpResponse response = getExecChainHandler().execute(request, scope, chain);
        if (active) {
            final MockRequest recordoRequest = mapper.toRecordoRequest(request);
            final MockResponse recordoResponse = mapper.toRecordoResponse(response);
            return mapper.toHttpResponse(onResponse.apply(recordoRequest, recordoResponse));
        } else {
            return response;
        }
    }

}
