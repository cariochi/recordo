package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.mockserver.interceptors.apache.ApacheMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.cariochi.recordo.core.utils.BeanUtils.findBean;


@UtilityClass
public class HttpClientInterceptors {

    public MockServerInterceptor of(ExtensionContext context) {
        return restTemplate(context)
                .or(() -> okHttpClient(context))
                .or(() -> apacheHttpClient(context))
                .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
    }

    private Optional<MockServerInterceptor> restTemplate(ExtensionContext context) {
        try {
            return findBean(RestTemplate.class, context)
                    .map(RestTemplate.class::cast)
                    .map(RestTemplateInterceptor::attachTo);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private Optional<MockServerInterceptor> okHttpClient(ExtensionContext context) {
        try {
            return findBean(OkMockServerInterceptor.class, context)
                    .or(() -> findBean(OkHttpClient.class, context).map(OkHttpClient.class::cast).map(OkMockServerInterceptor::attachTo))
                    .map(MockServerInterceptor.class::cast);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    private Optional<MockServerInterceptor> apacheHttpClient(ExtensionContext context) {
        try {
            return findBean(HttpClient.class, context)
                    .map(HttpClient.class::cast)
                    .map(ApacheMockServerInterceptor::new);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

}
