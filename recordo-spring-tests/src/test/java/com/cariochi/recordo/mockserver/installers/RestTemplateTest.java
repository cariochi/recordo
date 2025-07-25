package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.RecordoMockServer;
import com.cariochi.recordo.mockserver.installers.configs.MultipleRestTemplatesConfig;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateInstaller;
import com.cariochi.recordo.mockserver.interceptors.resttemplate.RestTemplateRecordoInterceptor;
import com.cariochi.recordo.read.Read;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = MultipleRestTemplatesConfig.class)
@ExtendWith(RecordoExtension.class)
class RestTemplateTest {

    @Autowired
    protected GitHub gitHub;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @MockServer("/mockserver/resttemplate/should_retrieve_gists.rest.json")
    void should_retrieve_gists() {
        final List<GistResponse> gists = gitHub.getGists();
        assertAsJson(gists)
                .isEqualTo("/mockserver/gists.json");
    }

    @Test
    @MockServer("/mockserver/resttemplate/should_create_gist.rest.json")
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

    @Test
    void should_get_exception() {
        assertThatThrownBy(() -> {
            final RestTemplateRecordoInterceptor interceptor = new RestTemplateRecordoInterceptor();
            try (RestTemplateInstaller installer = new RestTemplateInstaller(restTemplate).install(interceptor);
                    RecordoMockServer mockServer = new RecordoMockServer(interceptor, "/mockserver/resttemplate/several_requests.rest.json")
            ) {
                gitHub.getGists();
            }
        })
                .isInstanceOf(AssertionError.class)
                .hasMessage("Not all mocks requests were called");
    }

    @Test
    @MockServer("/mockserver/resttemplate/empty.rest.json")
    void should_have_all_interceptors() {
        assertThat(restTemplate.getInterceptors()).hasSize(1);
    }

    @Test
    void should_have_no_interceptors() {
        assertThat(restTemplate.getInterceptors()).isEmpty();
    }

}
