package com.cariochi.recordo.mockserver.interceptors.resttemplate;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class RestTemplateInstaller implements InterceptorInstaller {

    private final RestTemplate restTemplate;
    private RestTemplateRecordoInterceptor interceptor;

    public RestTemplateInstaller install(RestTemplateRecordoInterceptor interceptor) {
        this.interceptor = interceptor;
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        }
        restTemplate.getInterceptors().add(interceptor);
        return this;
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        this.interceptor.init(handler);
    }

    @Override
    public void uninstall() {
        restTemplate.getInterceptors().remove(interceptor);
    }
}
