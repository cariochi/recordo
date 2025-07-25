package com.cariochi.recordo.mockserver.interceptors.config;

import com.cariochi.recordo.mockserver.interceptors.restclient.RestClientRecordoInterceptor;
import com.cariochi.recordo.mockserver.restclient.GitHubRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

public class RestClientConfig {

    @Value("${github.key}")
    private String gitHubKey;

    @Bean
    public RestClientRecordoInterceptor recordoInterceptor() {
        return new RestClientRecordoInterceptor();
    }

    @Bean
    public GitHubRestClient gitHubRestClient(RestClientRecordoInterceptor recordoInterceptor) {
        final RestClient restClient = RestClient.builder()
                .baseUrl("https://api.github.com/gists")
                .defaultHeader("Authorization", "Bearer " + gitHubKey)
                .requestInterceptor(recordoInterceptor)
                .build();
        return new GitHubRestClient(restClient);
    }

}
