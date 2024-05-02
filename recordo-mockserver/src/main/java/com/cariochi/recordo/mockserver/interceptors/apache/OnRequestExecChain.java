package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class OnRequestExecChain implements ExecChainHandler {

    private final ExecChainHandler execChainHandler;
    private final ApacheMapper mapper = new ApacheMapper();

    private Function<MockRequest, Optional<MockResponse>> onRequest;

    public void onRequest(Function<MockRequest, Optional<MockResponse>> onRequest) {
        this.onRequest = onRequest;
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request,
                                       ExecChain.Scope scope,
                                       ExecChain chain) throws IOException, HttpException {
        final Optional<MockResponse> recordoResponse = onRequest.apply(mapper.toRecordoRequest(request));
        return recordoResponse.isPresent()
                ? mapper.toHttpResponse(recordoResponse.get())
                : execChainHandler.execute(request, scope, chain);
    }

}
