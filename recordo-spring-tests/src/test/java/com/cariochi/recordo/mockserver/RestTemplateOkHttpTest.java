package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.config.Profiles;
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

@SpringBootTest
@ActiveProfiles({Profiles.REST_TEMPLATE, Profiles.OK_HTTP})
@ExtendWith(RecordoExtension.class)
class RestTemplateOkHttpTest {

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockServer("/mockserver/resttemplate-okhttp/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHub.getGists();
        assertAsJson(gists)
                .isEqualTo("/mockserver/gists.json");
    }

    @Test
    @MockServer("/mockserver/resttemplate-okhttp/should_create_gist.rest.json")
    void should_create_gist(
            @Read("/mockserver/gist.json") Gist gist
    ) {
        final GistResponse response = gitHub.createGist(gist);
        final GistResponse updateResponse = gitHub.updateGist(response.getId(), gist);
        final Gist createdGist = gitHub.getGist(response.getId(), "hello world");
        gitHub.deleteGist(response.getId());

        assertAsJson(createdGist)
                .isEqualTo("/mockserver/gist.json");
    }


}
