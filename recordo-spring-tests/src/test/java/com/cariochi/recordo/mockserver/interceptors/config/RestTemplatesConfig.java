package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInterceptor;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class RestTemplatesConfig {

    @Value("${github.key}")
    private String gitHubKey;

    @Bean
    public RestTemplateInterceptor recordoInterceptor() {
        return new RestTemplateInterceptor();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateInterceptor recordoInterceptor) {
        return new RestTemplateBuilder().interceptors(recordoInterceptor).build();
    }

    @Bean
    public GitHub gitHub(RestTemplate restTemplate) {
        return new GitHubRestTemplate(restTemplate, gitHubKey);
    }

}
