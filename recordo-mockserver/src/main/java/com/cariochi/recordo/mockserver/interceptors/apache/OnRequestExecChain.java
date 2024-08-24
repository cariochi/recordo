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
import java.util.Optional;
import java.util.function.Function;

public class OnRequestExecChain extends AbstractExecChainHandler {

    @Setter
    private Function<MockRequest, Optional<MockResponse>> onRequest;

    public OnRequestExecChain(ExecChainHandler execChainHandler) {
        super(execChainHandler);
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request,
                                       ExecChain.Scope scope,
                                       ExecChain chain) throws IOException, HttpException {
        final Optional<MockResponse> recordoResponse = onRequest.apply(mapper.toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? mapper.toHttpResponse(recordoResponse.get())
                : getExecChainHandler().execute(request, scope, chain);
    }

}
