package com.cariochi.recordo.httpmock.http;

import com.cariochi.recordo.annotation.EnableRecordo;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;

import java.util.Optional;

import static com.cariochi.recordo.utils.Reflection.checkClassLoaded;

@UtilityClass
public class HttpClientInterceptors {

    public static HttpClientInterceptor of(Object testInstance) {
        return okHttpClientInterceptor(testInstance)
                .map(Optional::of)
                .orElseGet(() -> apacheHttpClientInterceptor(testInstance))
                .orElseThrow(() -> new IllegalArgumentException("Http Client not found"));
    }

    private static Optional<HttpClientInterceptor> okHttpClientInterceptor(Object testInstance) {
        try {
            checkClassLoaded("okhttp3.OkHttpClient");
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

    private static Optional<OkHttpClient> okHttpClient(Object testInstance) {
        try {
            checkClassLoaded("okhttp3.OkHttpClient");
            return Fields.of(testInstance)
                    .withTypeAndAnnotation(OkHttpClient.class, EnableRecordo.class).stream().findAny()
                    .map(TargetField::getValue);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private static Optional<HttpClientInterceptor> apacheHttpClientInterceptor(Object testInstance) {
        try {
            checkClassLoaded("org.apache.http.client.HttpClient");
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
