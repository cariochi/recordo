package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateRecordoInterceptor;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class RestTemplatesConfig {

    @Value("${github.key}")
    private String gitHubKey;

    @Bean
    public RestTemplateRecordoInterceptor recordoInterceptor() {
        return new RestTemplateRecordoInterceptor();
    }

    @Bean
    public GitHub gitHub(RestTemplateRecordoInterceptor recordoInterceptor) {
        final RestTemplate restTemplate = new RestTemplateBuilder().interceptors(recordoInterceptor).build();
        return new GitHubRestTemplate(restTemplate, gitHubKey);
    }

}
