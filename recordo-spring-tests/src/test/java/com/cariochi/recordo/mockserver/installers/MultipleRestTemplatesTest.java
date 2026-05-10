package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.installers.configs.MultipleRestTemplatesConfig;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = MultipleRestTemplatesConfig.class)
@ExtendWith(RecordoExtension.class)
class MultipleRestTemplatesTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("secondRestTemplate")
    private RestTemplate secondRestTemplate;

    @Autowired
    @Qualifier("thirdRestTemplate")
    private RestTemplate thirdRestTemplate;

    @Value("${github.key}")
    private String key;

    @Test
    @MockServer(client = "restTemplate", value = "/mockserver/multiple-rest-templates/first.rest.json")
    @MockServer(client = "secondRestTemplate", value = "/mockserver/multiple-rest-templates/second.rest.json")
    @MockServer(client = "thirdRestTemplate", value = "/mockserver/multiple-rest-templates/third.rest.json")
    void should_use_multiple_rest_templates() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(restTemplate, key);
        simpleGitHubClient.getGists();

        final GitHubRestTemplate secondGitHubClient = new GitHubRestTemplate(secondRestTemplate, key);
        secondGitHubClient.getGists();

        final GitHubRestTemplate thirdGitHubClient = new GitHubRestTemplate(thirdRestTemplate, key);
        thirdGitHubClient.getGists();
    }

    @Test
    @MockServer("/mockserver/multiple-rest-templates/first.rest.json")
    void should_use_default_rest_template() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(restTemplate, key);
        simpleGitHubClient.getGists();
    }

}
