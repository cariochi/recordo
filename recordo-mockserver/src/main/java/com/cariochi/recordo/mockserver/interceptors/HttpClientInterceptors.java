package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.web.client.RestTemplate;


@UtilityClass
public class HttpClientInterceptors {

    public Optional<MockServerInterceptor> findInterceptor(String beanName, ExtensionContext context) {
        try {
            final Beans beans = Beans.of(context);
            return Optional.<MockServerInterceptor>empty()
                    .or(() -> restTemplate(beanName, beans))
                    .or(() -> okHttpClient(beanName, beans))
                    .or(() -> apacheHttpClient(beanName, beans));
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private Optional<RestTemplateInterceptor> restTemplate(String beanName, Beans beans) {
        try {
            return beans.find(beanName, RestTemplateInterceptor.class)
                    .or(() -> beans.find(beanName, RestTemplate.class).map(RestTemplateInterceptor::attachTo));
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private Optional<OkMockServerInterceptor> okHttpClient(String beanName, Beans beans) {
        try {
            return beans.find(beanName, OkMockServerInterceptor.class)
                    .or(() -> beans.find(beanName, OkHttpClient.class).map(OkMockServerInterceptor::attachTo));
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private Optional<ApacheMockServerInterceptor> apacheHttpClient(String beanName, Beans beans) {
        try {
            return beans.find(beanName, ApacheMockServerInterceptor.class)
                    .or(() -> beans.find(beanName, HttpClient.class).map(ApacheMockServerInterceptor::new));
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

}
