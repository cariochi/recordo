package com.cariochi.recordo.mockserver.installers.configs;

import com.cariochi.recordo.mockserver.restclient.GitHubRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class RestClientConfig {

    @Bean
    public RestClient restClient(@Value("${github.key}") String key) {
        return RestClient.builder()
                .baseUrl("https://api.github.com/gists")
                .defaultHeader("Authorization", "Bearer " + key)
                .build();
    }

    @Bean
    public GitHubRestClient gitHubRestClient(RestClient restClient) {
        return new GitHubRestClient(restClient);
    }

}
