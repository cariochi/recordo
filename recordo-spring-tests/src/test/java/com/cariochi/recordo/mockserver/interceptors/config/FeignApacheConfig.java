package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheRecordoInterceptor;
import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@EnableFeignClients(clients = GitHubFeign.class)
public class FeignApacheConfig {

    @Bean
    public ApacheRecordoInterceptor recordoInterceptor() {
        return new ApacheRecordoInterceptor();
    }

    @Bean
    public Client apacheFeignClient(ApacheRecordoInterceptor recordoInterceptor) {
        final CloseableHttpClient client = HttpClients.custom()
                .addExecInterceptorFirst("recordoInterceptor", recordoInterceptor)
                .build();
        return new ApacheHttp5Client(client);
    }

}
