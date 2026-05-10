package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
import com.cariochi.recordo.mockserver.interceptors.apache.ApacheInterceptor;
import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@EnableAutoConfiguration
@EnableFeignClients(clients = GitHubFeign.class)
public class FeignApacheConfig {

    @Primary
    @Bean
    public HttpClient httpClient() {
        return HttpClients.custom()
                .addExecInterceptorFirst("recordoInterceptor", new ApacheInterceptor())
                .build();
    }

    @Bean
    public Client apacheFeignClient(HttpClient httpClient) {
        return new ApacheHttp5Client(httpClient);
    }

}
