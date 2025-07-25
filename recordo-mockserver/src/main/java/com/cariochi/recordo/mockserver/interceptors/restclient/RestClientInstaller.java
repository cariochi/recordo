package com.cariochi.recordo.mockserver.interceptors.restclient;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import com.cariochi.reflecto.invocations.model.Reflection;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import static com.cariochi.reflecto.Reflecto.reflect;

@RequiredArgsConstructor
public class RestClientInstaller implements InterceptorInstaller {

    private final RestClient restClient;
    private RestClientRecordoInterceptor interceptor;

    public RestClientInstaller install(RestClientRecordoInterceptor interceptor) {
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
    public void init(RecordoRequestHandler handler) {
        interceptor.init(handler);
    }

    @Override
    public void uninstall() {
        reflect(restClient).perform("interceptors.remove(?)", interceptor);
    }
}
