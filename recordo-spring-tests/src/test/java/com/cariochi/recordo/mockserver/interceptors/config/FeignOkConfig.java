package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInterceptor;
import feign.Client;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@EnableFeignClients(clients = GitHubFeign.class)
public class FeignOkConfig {

    @Bean
    public OkhttpInterceptor recordoInterceptor() {
        return new OkhttpInterceptor();
    }

    @Bean("MY-BEAN")
    public OkHttpClient okHttpClient(OkhttpInterceptor recordoInterceptor) {
        return new OkHttpClient.Builder().addInterceptor(recordoInterceptor).build();
    }

    @Bean
    public Client okFeignClient(OkHttpClient client) {
        return new feign.okhttp.OkHttpClient(client);
    }

}
