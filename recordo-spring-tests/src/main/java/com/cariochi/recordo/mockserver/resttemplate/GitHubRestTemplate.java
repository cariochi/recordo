package com.cariochi.recordo.mockserver.resttemplate;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.RequestEntity.delete;
import static org.springframework.http.RequestEntity.get;
import static org.springframework.http.RequestEntity.patch;
import static org.springframework.http.RequestEntity.post;

public class GitHubRestTemplate implements GitHub {

    private static final String URL = "https://api.github.com/gists";

    private final RestTemplate restTemplate;

    private final String key;

    public GitHubRestTemplate(RestTemplate restTemplate, @Value("${github.key}") String key) {
        this.restTemplate = restTemplate;
        this.key = key;
    }

    @Override
    public List<GistResponse> getGists() {
        return restTemplate.exchange(
                get(URI.create(URL)).header("Authorization", "Bearer " + key).build(),
                new ParameterizedTypeReference<List<GistResponse>>() {
                }
        ).getBody();
    }

    @Override
    public Gist getGist(String id, String rand) {
        return restTemplate.exchange(
                get(URI.create(URL + "/" + id)).header("Authorization", "Bearer " + key).build(),
                Gist.class
        ).getBody();
    }

    @Override
    public GistResponse createGist(Gist gist) {
        return restTemplate.exchange(
                post(URI.create(URL)).header("Authorization", "Bearer " + key).body(gist),
                GistResponse.class
        ).getBody();
    }

    @Override
    public GistResponse updateGist(String id, Gist gist) {
        return restTemplate.exchange(
                patch(URI.create(URL + "/" + id)).header("Authorization", "Bearer " + key).body(gist),
                GistResponse.class
        ).getBody();
    }

    @Override
    public void deleteGist(String id) {
        restTemplate.exchange(
                delete(URI.create(URL + "/" + id)).header("Authorization", "Bearer " + key).build(),
                Void.class
        );
    }

}
