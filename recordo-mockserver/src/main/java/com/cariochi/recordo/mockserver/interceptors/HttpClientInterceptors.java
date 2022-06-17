package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import com.cariochi.reflecto.fields.JavaField;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.ClassUtils;
import org.apache.http.client.HttpClient;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;


@UtilityClass
public class HttpClientInterceptors {

    public MockServerInterceptor of(Object testInstance) {
        return restTemplateInterceptor(testInstance)
                .or(() -> okHttpClientInterceptor(testInstance))
                .or(() -> apacheHttpClientInterceptor(testInstance))
                .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
    }

    private Optional<MockServerInterceptor> restTemplateInterceptor(Object testInstance) {
        try {
            ClassUtils.getClass("org.springframework.web.client.RestTemplate", false);
            return reflect(testInstance).fields().includeEnclosing()
                    .withTypeAndAnnotation(RestTemplate.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue)
                    .map(RestTemplate.class::cast)
                    .map(RestTemplateInterceptor::attachTo);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<MockServerInterceptor> okHttpClientInterceptor(Object testInstance) {
        try {
            ClassUtils.getClass("okhttp3.OkHttpClient", false);
            return reflect(testInstance).fields().includeEnclosing()
                    .withTypeAndAnnotation(OkMockServerInterceptor.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue)
                    .or(() -> okHttpClient(testInstance).map(OkMockServerInterceptor::attachTo))
                    .map(MockServerInterceptor.class::cast);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<OkHttpClient> okHttpClient(Object testInstance) {
        try {
            ClassUtils.getClass("okhttp3.OkHttpClient", false);
            return reflect(testInstance).fields().includeEnclosing()
                    .withTypeAndAnnotation(OkHttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<MockServerInterceptor> apacheHttpClientInterceptor(Object testInstance) {
        try {
            ClassUtils.getClass("org.apache.http.client.HttpClient", false);
            return reflect(testInstance).fields().includeEnclosing()
                    .withTypeAndAnnotation(HttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue)
                    .map(HttpClient.class::cast)
                    .map(ApacheMockServerInterceptor::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

}
