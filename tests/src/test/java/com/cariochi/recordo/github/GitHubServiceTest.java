package com.cariochi.recordo.github;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.github.dto.GistDto;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.RecordoMockServer;
import com.cariochi.recordo.mockserver.interceptors.okhttp.OkMockServerInterceptor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@ExtendWith(RecordoExtension.class)
class GitHubServiceTest {

    @EnableRecordo
    private final OkHttpClient client = new OkHttpClient();
    private final GitHubService service = new GitHubService(client);

    @Test
    @MockServer("/mockserver/gists.mock.json")
    void test_mock_server() {
        final List<GistDto> gists = service.getGists();
        assertAsJson(gists).isEqualTo("/mockserver/output.json");
    }

    @Test
    void test_mock_http_with_variables() {
        try (RecordoMockServer mockServer =
                     new RecordoMockServer("/mockserver/gists_with_variables.mock.json", OkMockServerInterceptor.attachTo(client))) {

            mockServer.set("id1", "36387e79b940de553ad0b381afc29bf4");
            mockServer.set("id2", "cc7e0f8678d69196387b623bd45f0f33");
            mockServer.set("id3", "14c814e5561e8f03fce5f6d815af706c");
            final List<GistDto> gists = service.getGists();

            assertAsJson(gists).isEqualTo("/mockserver/output.json");
        }
    }

}
