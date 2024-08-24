package com.cariochi.recordo.config;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.config.Profiles.REST_TEMPLATE;
import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@Profile(REST_TEMPLATE)
@Slf4j
public class RestTemplateConfig {

    @Bean
    public GitHub gitHub(RestTemplate restTemplate, @Value("${github.key}") String key) {
        return new GitHubRestTemplate(restTemplate, key);
    }

    @Bean
    public RestTemplate restTemplate() {
        return newRestTemplate();
    }

    @Bean
    public RestTemplate secondRestTemplate() {
        return newRestTemplate();
    }

    @Bean
    public RestTemplate thirdRestTemplate() {
        return newRestTemplate();
    }

    private static RestTemplate newRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, requestBody, execution) -> {
            final ClientHttpResponse response = execution.execute(request, requestBody);
            String responseBody = IOUtils.toString(response.getBody(), UTF_8);
            log.debug("\n\nRequest: {} {} {}\nResponse: {} {}\n", request.getMethod(), request.getURI(), requestBody, response.getStatusCode(), responseBody);
            return response;
        });
        return restTemplate;
    }

}
