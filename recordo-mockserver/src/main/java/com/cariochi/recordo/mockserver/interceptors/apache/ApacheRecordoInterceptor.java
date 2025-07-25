package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.io.IOException;
import java.util.Optional;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChain.Scope;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

public class ApacheRecordoInterceptor implements ExecChainHandler, RecordoInterceptor {

    protected final ApacheMapper mapper = new ApacheMapper();
    private RecordoRequestHandler handler;

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, Scope scope, ExecChain execChain) throws IOException, HttpException {
        final MockRequest recordoRequest = mapper.toRecordoRequest(request);
        final Optional<MockResponse> response = handler.onRequest(recordoRequest);
        if (response.isPresent()) {
            return mapper.toHttpResponse(response.get());
        } else {
            final ClassicHttpResponse httpResponse = execChain.proceed(request, scope);
            final MockResponse recordoResponse = mapper.toRecordoResponse(httpResponse);
            return mapper.toHttpResponse(handler.onResponse(recordoRequest, recordoResponse));
        }
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        this.handler = handler;
    }
}
