package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import com.cariochi.recordo.read.Read;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static com.cariochi.recordo.config.Profiles.FEIGN;
import static com.cariochi.recordo.config.Profiles.OK_HTTP;

@SpringBootTest
@ActiveProfiles({FEIGN, OK_HTTP})
@ExtendWith(RecordoExtension.class)
class FeignOkHttpTest {

    @Autowired
    private OkHttpClient client;

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockServer("/mockserver/feign-okhttp/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHub.getGists();
        assertAsJson(gists)
                .isEqualTo("/mockserver/gists.json");
    }

    @Test
    void should_create_gist(
            @Read("/mockserver/gist.json") Gist gist
    ) {
        try (OkMockServerInterceptor interceptor = new OkMockServerInterceptor(client);
             RecordoMockServer mockServer = new RecordoMockServer(interceptor, "/mockserver/feign-okhttp/should_create_gist.rest.json")
        ) {

            mockServer.set("gistId", "16d0b491b237960fd5bf3ba503a3d18b");

            final GistResponse response = gitHub.createGist(gist);
            final GistResponse updateResponse = gitHub.updateGist(response.getId(), gist);
            final Gist createdGist = gitHub.getGist(response.getId(), "hello world");
            gitHub.deleteGist(response.getId());

            assertAsJson(createdGist)
                    .isEqualTo("/mockserver/gist.json");
        }
    }

}
