package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.installers.configs.FeignApacheConfig;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.read.Read;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@SpringBootTest(classes = FeignApacheConfig.class)
@ExtendWith(RecordoExtension.class)
class FeignApacheTest {

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockServer("/mockserver/feign-apache/should_retrieve_gists.rest.json")
    @MockServer("/mockserver/feign-apache/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHub.getGists();
        final List<GistResponse> gists2 = gitHub.getGists();
        assertAsJson(gists)
                .isEqualTo("/mockserver/gists.json");
    }

    @Test
    @MockServer("/mockserver/feign-apache/should_create_gist.rest.json")
    void should_create_gist(
            @Read("/mockserver/gist.json") Gist gist
    ) {
        final GistResponse response = gitHub.createGist(gist);
        final Gist created = gitHub.getGist(response.getId(), "hello world");
        gitHub.deleteGist(response.getId());
        assertAsJson(created)
                .isEqualTo("/mockserver/gist.json");
    }

}
