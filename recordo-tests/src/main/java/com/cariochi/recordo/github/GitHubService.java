package com.cariochi.recordo.github;

import com.cariochi.recordo.github.dto.GistDto;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;

import java.util.List;

public class GitHubService {

    private final GitHubClient github;

    public GitHubService(okhttp3.OkHttpClient client) {
        github = Feign.builder()
                .client(new OkHttpClient(client))
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(GitHubClient.class, "https://api.github.com");
    }

    public List<GistDto> getGists() {
        return github.getGists();
    }

}
