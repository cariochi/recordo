package com.cariochi.recordo.mockserver.installers.configs;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class MultipleRestTemplatesConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public GitHub gitHub(RestTemplate restTemplate, @Value("${github.key}") String key) {
        return new GitHubRestTemplate(restTemplate, key);
    }

    @Bean
    public RestTemplate secondRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate thirdRestTemplate() {
        return new RestTemplate();
    }
}
