package com.cariochi.recordo.github;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.github.dto.GistDto;
import com.cariochi.recordo.mockserver.MockServer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.cariochi.recordo.assertions.JsonAssertion.assertAsJson;

@ExtendWith(RecordoExtension.class)
class NestedGitHubServiceTest {

    @EnableRecordo
    private final OkHttpClient client = new OkHttpClient();
    private final GitHubService service = new GitHubService(client);

    @Nested
    class NestedTest {

        @Test
        @MockServer("/mockserver/gists.mock.json")
        void test_mock_server() {
            final List<GistDto> gists = service.getGists();
            assertAsJson(gists).isEqualTo("/mockserver/output.json");
        }

    }

}
