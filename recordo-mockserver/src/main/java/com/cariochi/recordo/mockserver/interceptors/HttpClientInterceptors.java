package com.cariochi.recordo.mockserver.interceptors;

import com.cariochi.recordo.mockserver.interceptors.apache.ApacheMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import java.util.Map;
import java.util.Map.Entry;
import lombok.experimental.UtilityClass;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.core.utils.BeanUtils.findBeans;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;


@UtilityClass
public class HttpClientInterceptors {


    public Map<String, ? extends MockServerInterceptor> findAll(ExtensionContext context) {

        final Map<String, ? extends MockServerInterceptor> restTemplates = restTemplate(context);
        if (!restTemplates.isEmpty()) {
            return restTemplates;
        }

        final Map<String, ? extends MockServerInterceptor> okHttpClients = okHttpClient(context);
        if (!okHttpClients.isEmpty()) {
            return okHttpClients;
        }

        final Map<String, ? extends MockServerInterceptor> apacheHttpClients = apacheHttpClient(context);
        if (!apacheHttpClients.isEmpty()) {
            return apacheHttpClients;
        }

        return emptyMap();
    }

    private Map<String, ? extends MockServerInterceptor> restTemplate(ExtensionContext context) {
        try {
            return findBeans(RestTemplate.class, context).entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> RestTemplateInterceptor.attachTo(e.getValue())));
        } catch (Exception | NoClassDefFoundError e) {
            return emptyMap();
        }
    }

    private Map<String, ? extends MockServerInterceptor> okHttpClient(ExtensionContext context) {
        try {
            final Map<String, ? extends MockServerInterceptor> beans = findBeans(OkMockServerInterceptor.class, context);
            if (!beans.isEmpty()) {
                return beans;
            } else {
                return findBeans(OkHttpClient.class, context).entrySet().stream()
                        .collect(toMap(Entry::getKey, e -> OkMockServerInterceptor.attachTo(e.getValue())));
            }
        } catch (Exception | NoClassDefFoundError e) {
            return emptyMap();
        }
    }

    private Map<String, ? extends MockServerInterceptor> apacheHttpClient(ExtensionContext context) {
        try {
            return findBeans(HttpClient.class, context).entrySet().stream()
                    .collect(toMap(Map.Entry::getKey, e -> new ApacheMockServerInterceptor(e.getValue())));
        } catch (Exception | NoClassDefFoundError e) {
            return emptyMap();
        }
    }

}
