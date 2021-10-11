package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheHttpClientInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkHttpClientInterceptor;
import com.cariochi.recordo.utils.reflection.ClassLoaders;
import com.cariochi.reflecto.fields.JavaField;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;

import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;


@UtilityClass
public class HttpClientInterceptors {

    public HttpClientInterceptor of(Object testInstance) {
        return okHttpClientInterceptor(testInstance)
                .map(Optional::of)
                .orElseGet(() -> apacheHttpClientInterceptor(testInstance))
                .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
    }

    private Optional<HttpClientInterceptor> okHttpClientInterceptor(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("okhttp3.OkHttpClient");
            return reflect(testInstance).fields()
                    .withTypeAndAnnotation(OkHttpClientInterceptor.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue)
                    .or(() -> okHttpClient(testInstance).map(OkHttpClientInterceptor::attachTo))
                    .map(HttpClientInterceptor.class::cast);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<OkHttpClient> okHttpClient(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("okhttp3.OkHttpClient");
            return reflect(testInstance).fields()
                    .withTypeAndAnnotation(OkHttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<HttpClientInterceptor> apacheHttpClientInterceptor(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("org.apache.http.client.HttpClient");
            return reflect(testInstance).fields()
                    .withTypeAndAnnotation(HttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(JavaField::getValue)
                    .map(HttpClient.class::cast)
                    .map(ApacheHttpClientInterceptor::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
