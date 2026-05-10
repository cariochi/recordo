package com.cariochi.recordo.mockserver.interceptors.resttemplate;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@RequiredArgsConstructor
public class RestTemplateInstaller implements InterceptorInstaller<RestTemplateInterceptor> {

    private final RestTemplate restTemplate;
    private RestTemplateInterceptor interceptor;

    @Override
    public RestTemplateInstaller install(RestTemplateInterceptor interceptor) {
        this.interceptor = interceptor;
        final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        }
        restTemplate.getInterceptors().add(interceptor);
        return this;
    }

    @Override
    public Optional<RecordoInterceptor> findInterceptor() {
        return restTemplate.getInterceptors().stream()
                .filter(RestTemplateInterceptor.class::isInstance)
                .map(RecordoInterceptor.class::cast)
                .findFirst();
    }

    @Override
    public void setHandler(RecordoRequestHandler handler) {
        interceptor.setHandler(handler);
    }

    @Override
    public void uninstall() {
        restTemplate.getInterceptors().remove(interceptor);
    }
}
