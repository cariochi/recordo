package com.cariochi.recordo.mockserver.interceptors.resttemplate;

import com.cariochi.recordo.mockserver.interceptors.MockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Collections.emptyList;

public class RestTemplateInterceptor implements MockServerInterceptor, ClientHttpRequestInterceptor {

    private final RestTemplate restTemplate;
    private final RestTemplateMapper mapper = new RestTemplateMapper();

    private RecordoRequestHandler handler;

    public RestTemplateInterceptor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        final List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        restTemplate.setInterceptors(emptyList());
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        }
        restTemplate.setInterceptors(interceptors);

        restTemplate.getInterceptors().add(this);
    }

    @Override
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

    @Override
    public void close() {
        restTemplate.getInterceptors().remove(this);
    }
}
