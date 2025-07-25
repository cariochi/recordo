package com.cariochi.recordo.mockserver.installers.configs;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
import feign.Client;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@EnableFeignClients(clients = GitHubFeign.class)
public class FeignOkConfig {

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }

    @Bean
    public Client okFeignClient(OkHttpClient httpClient) {
        return new feign.okhttp.OkHttpClient(httpClient);
    }

}
