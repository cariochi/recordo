package com.cariochi.recordo.config;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import okhttp3.OkHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.config.Profiles.REST_TEMPLATE;
import static com.cariochi.recordo.config.Profiles.SIMPLE;

@Configuration
@Profile(REST_TEMPLATE)
@Import(ApplicationConfig.class)
public class RestTemplateConfig {

    @Bean
    public GitHub gitHub(RestTemplate restTemplate) {
        return new GitHubRestTemplate(restTemplate);
    }

    @Bean
    @ConditionalOnBean(CloseableHttpClient.class)
    public RestTemplate apacheRestTemplate(CloseableHttpClient client) {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
    }

    @Bean
    @ConditionalOnBean(OkHttpClient.class)
    public RestTemplate restTemplate(OkHttpClient client) {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(client));
    }

    @Bean
    @Profile(SIMPLE)
    public RestTemplate simpleRestTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory(new OkHttpClient()));
    }

}
