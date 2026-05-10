package com.cariochi.recordo.mockserver.interceptors.restclient;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.reflecto.invocations.model.Reflection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;

@RequiredArgsConstructor
public class RestClientInstaller implements InterceptorInstaller<RestClientInterceptor> {

    private final RestClient restClient;
    private RestClientInterceptor interceptor;

    @Override
    public RestClientInstaller install(RestClientInterceptor interceptor) {
        this.interceptor = interceptor;
        final Reflection reflect = reflect(this.restClient);
        final ClientHttpRequestFactory requestFactory = reflect.perform("clientRequestFactory");
        if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
            reflect.perform("clientRequestFactory=?", new BufferingClientHttpRequestFactory(requestFactory));
        }
        List<ClientHttpRequestInterceptor> interceptors = reflect.perform("interceptors");
        if (interceptors == null) {
            interceptors = new ArrayList<>();
            reflect.perform("interceptors=?", interceptors);
        }
        interceptors.add(interceptor);
        return this;
    }

    @Override
    public Optional<RecordoInterceptor> findInterceptor() {
        final Reflection reflect = reflect(restClient);
        List<ClientHttpRequestInterceptor> interceptors = reflect.perform("interceptors");
        return Optional.ofNullable(interceptors).stream()
                .flatMap(List::stream)
                .filter(RestClientInterceptor.class::isInstance)
                .map(RecordoInterceptor.class::cast)
                .findFirst();
    }

    @Override
    public void setHandler(RecordoRequestHandler handler) {
        interceptor.setHandler(handler);
    }

    @Override
    public void uninstall() {
        reflect(restClient).perform("interceptors.remove(?)", interceptor);
    }
}
