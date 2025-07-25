package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.GitHub;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.installers.configs.FeignOkConfig;
import com.cariochi.recordo.mockserver.dto.Gist;
import com.cariochi.recordo.mockserver.dto.GistResponse;
import com.cariochi.recordo.read.Read;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FeignOkConfig.class)
@ExtendWith(RecordoExtension.class)
class ReadFromFolderTest {

    @Autowired
    private OkHttpClient client;

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockServer("/mockserver/mock-folder/")
    void should_read_from_folder(
            @Read("/mockserver/gist.json") Gist gist
    ) {
        final GistResponse response = gitHub.createGist(gist);
        final GistResponse updateResponse = gitHub.updateGist(response.getId(), gist);
        final Gist createdGist = gitHub.getGist(response.getId(), "hello world");
        gitHub.deleteGist(response.getId());
    }

    @Test
    @MockServer("/mockserver/mock-file/all.json")
    void should_read_from_file(
            @Read("/mockserver/gist.json") Gist gist
    ) {
        final GistResponse response = gitHub.createGist(gist);
        final GistResponse updateResponse = gitHub.updateGist(response.getId(), gist);
        final Gist createdGist = gitHub.getGist(response.getId(), "hello world");
        gitHub.deleteGist(response.getId());
    }

}
