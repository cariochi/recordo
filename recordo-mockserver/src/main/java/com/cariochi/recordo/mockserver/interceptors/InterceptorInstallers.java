package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInstaller;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheRecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInstaller;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpRecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientInstaller;
import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientRecordoInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInstaller;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateRecordoInterceptor;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Slf4j
@UtilityClass
public class InterceptorInstallers {

    public static Optional<InterceptorInstaller> findInterceptor(String beanName, ExtensionContext context) {
        final Beans beans = Beans.of(context);
        return Optional.<InterceptorInstaller>empty()
                .or(() -> interceptors(beanName, beans))
                .or(() -> restClients(beanName, beans))
                .or(() -> restTemplate(beanName, beans))
                .or(() -> okHttpClient(beanName, beans))
                .or(() -> apacheHttpClient(beanName, beans));
    }

    private static Optional<InterceptorInstaller> interceptors(String beanName, Beans beans) {
        try {
            return beans.find(beanName, RecordoInterceptor.class)
                    .map(NoOpInstaller::new);
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private static Optional<InterceptorInstaller> restClients(String beanName, Beans beans) {
        try {
            return beans.find(beanName, RestClient.class)
                    .map(restClient -> new RestClientInstaller(restClient).install(new RestClientRecordoInterceptor()))
                    .map(InterceptorInstaller.class::cast);
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private static Optional<InterceptorInstaller> restTemplate(String beanName, Beans beans) {
        try {
            return beans.find(beanName, RestTemplate.class)
                    .map(restTemplate -> new RestTemplateInstaller(restTemplate).install(new RestTemplateRecordoInterceptor()))
                    .map(InterceptorInstaller.class::cast);
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private static Optional<InterceptorInstaller> okHttpClient(String beanName, Beans beans) {
        try {
            return beans.find(beanName, OkHttpClient.class)
                    .map(httpClient -> new OkhttpInstaller(httpClient).install(new OkhttpRecordoInterceptor()))
                    .map(InterceptorInstaller.class::cast);
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    private static Optional<InterceptorInstaller> apacheHttpClient(String beanName, Beans beans) {
        try {
            return beans.find(beanName, HttpClient.class)
                    .map(httpClient -> new ApacheInstaller(httpClient).install(new ApacheRecordoInterceptor()))
                    .map(InterceptorInstaller.class::cast);
        } catch (Exception | NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

}
