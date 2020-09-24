package com.cariochi.recordo.mockhttp.server.interceptors;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.mockhttp.server.interceptors.apache.ApacheHttpClientInterceptor;
import com.cariochi.recordo.mockhttp.server.interceptors.okhttp.OkHttpClientInterceptor;
import com.cariochi.recordo.utils.reflection.ClassLoaders;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;

import java.util.Optional;


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
            return Fields.of(testInstance)
                    .withTypeAndAnnotation(OkHttpClientInterceptor.class, EnableRecordo.class).stream().findAny()
                    .map(TargetField::getValue)
                    .map(Optional::of)
                    .orElseGet(() -> okHttpClient(testInstance).map(OkHttpClientInterceptor::new))
                    .map(HttpClientInterceptor.class::cast);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<OkHttpClient> okHttpClient(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("okhttp3.OkHttpClient");
            return Fields.of(testInstance)
                    .withTypeAndAnnotation(OkHttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(TargetField::getValue);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<HttpClientInterceptor> apacheHttpClientInterceptor(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("org.apache.http.client.HttpClient");
            return Fields.of(testInstance)
                    .withTypeAndAnnotation(HttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(TargetField::getValue)
                    .map(HttpClient.class::cast)
                    .map(ApacheHttpClientInterceptor::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
