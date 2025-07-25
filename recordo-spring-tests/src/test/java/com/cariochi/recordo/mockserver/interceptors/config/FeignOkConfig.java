package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.feign.GitHubFeign;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpRecordoInterceptor;
import feign.Client;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@EnableFeignClients(clients = GitHubFeign.class)
public class FeignOkConfig {

    @Bean("MY-BEAN")
    public OkhttpRecordoInterceptor recordoInterceptor() {
        return new OkhttpRecordoInterceptor();
    }

    @Bean
    public Client okFeignClient(OkhttpRecordoInterceptor recordoInterceptor) {
        final OkHttpClient client = new Builder().addInterceptor(recordoInterceptor).build();
        return new feign.okhttp.OkHttpClient(client);
    }

}
