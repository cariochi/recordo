package com.cariochi.recordo.config;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.config.Profiles.REST_TEMPLATE;

@Configuration
@Profile(REST_TEMPLATE)
public class RestTemplateConfig {

    @Bean
    public GitHub gitHub(RestTemplate restTemplate, @Value("${github.key}") String key) {
        return new GitHubRestTemplate(restTemplate, key);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
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
