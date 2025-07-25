package com.cariochi.recordo.mockserver.feign;

import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "GitHub", url = "https://api.github.com", configuration = GitHubFeign.Config.class)
public interface GitHubFeign extends GitHub {

    @GetMapping("/gists")
    List<GistResponse> getGists();

    @GetMapping("/gists/{id}")
    Gist getGist(@PathVariable("id") String id, @RequestParam("rand") String rand);

    @PostMapping("/gists")
    GistResponse createGist(Gist gist);

    @PatchMapping("/gists/{id}")
    GistResponse updateGist(@PathVariable("id") String id, Gist gist);

    @DeleteMapping("/gists/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteGist(@PathVariable("id") String id);

    class Config {

        @Bean
        public GitHubInterceptor authInterceptor() {
            return new GitHubInterceptor();
        }
    }

    class GitHubInterceptor implements RequestInterceptor {

        @Value("${github.key}")
        public String key;

        @Override
        public void apply(RequestTemplate requestTemplate) {
            requestTemplate.header("Authorization", "Bearer " + key);
        }

    }
}
