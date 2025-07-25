package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.RecordoMockServer;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.mockserver.installers.configs.FeignOkConfig;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpInstaller;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkhttpRecordoInterceptor;
import com.cariochi.recordo.read.Read;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@SpringBootTest(classes = FeignOkConfig.class)
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
        final OkhttpRecordoInterceptor interceptor = new OkhttpRecordoInterceptor();
        try (OkhttpInstaller installer = new OkhttpInstaller(client).install(interceptor);
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
