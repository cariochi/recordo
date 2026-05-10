package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientInterceptor;
import com.cariochi.recordo.mockserver.restclient.GitHubRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class RestClientConfig {

    @Value("${github.key}")
    private String gitHubKey;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com/gists")
                .defaultHeader("Authorization", "Bearer " + gitHubKey)
                .requestInterceptor(new RestClientInterceptor())
                .build();
    }

    @Bean
    public GitHubRestClient gitHubRestClient(RestClient restClient) {
        return new GitHubRestClient(restClient);
    }

}
