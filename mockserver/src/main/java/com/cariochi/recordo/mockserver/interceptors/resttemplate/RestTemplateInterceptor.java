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
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

public class RestTemplateInterceptor implements MockServerInterceptor, ClientHttpRequestInterceptor {

    private final RestTemplateMapper mapper = new RestTemplateMapper();

    private RecordoRequestHandler handler;

    public static RestTemplateInterceptor attachTo(RestTemplate restTemplate) {
        final List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors().stream()
                .filter(not(RestTemplateInterceptor.class::isInstance))
                .collect(toList());
        restTemplate.setInterceptors(emptyList());
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        }
        final RestTemplateInterceptor interceptor = new RestTemplateInterceptor();
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
        return interceptor;
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

}
