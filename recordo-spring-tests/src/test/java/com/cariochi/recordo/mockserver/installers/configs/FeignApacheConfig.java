package com.cariochi.recordo.mockserver.installers.configs;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
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
    public CloseableHttpClient httpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public Client apacheFeignClient(CloseableHttpClient httpClient) {
        return new ApacheHttp5Client(httpClient);
    }

}
