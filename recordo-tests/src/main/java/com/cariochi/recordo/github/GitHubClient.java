package com.cariochi.recordo.github;

import com.cariochi.recordo.github.dto.GistDto;
import feign.RequestLine;

import java.util.List;

public interface GitHubClient {

    @RequestLine("GET /gists?per_page=3&page=1")
    List<GistDto> getGists();

}
