package com.cariochi.recordo.mockserver;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static com.cariochi.recordo.config.Profiles.APACHE_HTTP;
import static com.cariochi.recordo.config.Profiles.OK_HTTP;
import static com.cariochi.recordo.config.Profiles.REST_TEMPLATE;

@SpringBootTest
@ActiveProfiles({REST_TEMPLATE, APACHE_HTTP, OK_HTTP})
@ExtendWith(RecordoExtension.class)
class MultipleRestTemplatesTest {

    @Autowired
    @EnableRecordo
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate secondRestTemplate;

    @Autowired
    private RestTemplate thirdRestTemplate;

    @Value("${github.key}")
    private String key;

    @Test
    @MockServer(httpClient = "restTemplate", value = "/mockserver/multiple-rest-templates/first.rest.json")
    @MockServer(httpClient = "secondRestTemplate", value = "/mockserver/multiple-rest-templates/second.rest.json")
    @MockServer(httpClient = "thirdRestTemplate", value = "/mockserver/multiple-rest-templates/third.rest.json")
    void should_use_multiple_rest_templates() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(restTemplate, key);
        simpleGitHubClient.getGists();

        final GitHubRestTemplate apacheGitHubClient = new GitHubRestTemplate(secondRestTemplate, key);
        apacheGitHubClient.getGists();

        final GitHubRestTemplate okHttpGitHubClient = new GitHubRestTemplate(thirdRestTemplate, key);
        okHttpGitHubClient.getGists();
    }

    @Test
    @MockServer("/mockserver/multiple-rest-templates/first.rest.json")
    void should_use_default_rest_template() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(restTemplate, key);
        simpleGitHubClient.getGists();
    }

}
