package com.cariochi.recordo.config;

import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.cariochi.recordo.config.Profiles.APACHE_HTTP;
import static com.cariochi.recordo.config.Profiles.OK_HTTP;

@Configuration
public class ApplicationConfig {

    @Bean
    @Profile(APACHE_HTTP)
    public CloseableHttpClient apacheHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    @Profile(OK_HTTP)
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

}
