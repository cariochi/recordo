package com.cariochi.recordo.config;

import feign.Client;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static com.cariochi.recordo.config.Profiles.FEIGN;

@Configuration
@Profile(FEIGN)
@Import(ApplicationConfig.class)
@EnableFeignClients("com.cariochi.recordo.mockserver.feign")
public class FeignConfig {

    @Bean
    @ConditionalOnBean(CloseableHttpClient.class)
    public Client apacheFeignClient(CloseableHttpClient client) {
        return new feign.hc5.ApacheHttp5Client(client);
    }

    @Bean
    @ConditionalOnBean(OkHttpClient.class)
    public Client okFeignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }

}
