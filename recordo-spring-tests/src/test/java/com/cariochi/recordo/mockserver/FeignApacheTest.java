package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.read.Read;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static com.cariochi.recordo.config.Profiles.APACHE_HTTP;
import static com.cariochi.recordo.config.Profiles.FEIGN;

@SpringBootTest
@ActiveProfiles({FEIGN, APACHE_HTTP})
@ExtendWith(RecordoExtension.class)
class FeignApacheTest {

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockServer("/mockserver/feign-apache/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHub.getGists();
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
