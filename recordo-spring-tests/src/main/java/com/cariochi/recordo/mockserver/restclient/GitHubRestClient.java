package com.cariochi.recordo.mockserver.restclient;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class GitHubRestClient implements GitHub {

    private final static ParameterizedTypeReference<List<GistResponse>> LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient client;

    @Override
    public List<GistResponse> getGists() {
        return client.get()
                .retrieve()
                .body(LIST_TYPE);
    }

    @Override
    public Gist getGist(String id, String rand) {
        return client.get()
                .uri("/{id}", id)
                .retrieve()
                .body(Gist.class);
    }

    @Override
    public GistResponse createGist(Gist gist) {
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(gist)
                .retrieve()
                .body(GistResponse.class);
    }

    @Override
    public GistResponse updateGist(String id, Gist gist) {
        return client.patch()
                .uri("/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(gist)
                .retrieve()
                .body(GistResponse.class);
    }

    @Override
    public void deleteGist(String id) {
        client.delete()
                .uri("/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
