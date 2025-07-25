package com.cariochi.recordo.mockserver.interceptors.resttemplate;

import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RestTemplateRecordoInterceptor implements RecordoInterceptor, ClientHttpRequestInterceptor {

    private final RestTemplateMapper mapper = new RestTemplateMapper();
    private RecordoRequestHandler handler;

    public void init(RecordoRequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        final MockRequest recordoRequest = mapper.toRecordoRequest(request, body);
        final MockResponse recordoResponse = handler.onRequest(recordoRequest)
                .orElseGet(() -> handler.onResponse(recordoRequest, mapper.toRecordoResponse(execute(request, body, execution))));
        return mapper.toHttpResponse(recordoResponse);
    }

    @SneakyThrows
    private ClientHttpResponse execute(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        return execution.execute(request, body);
    }

}
