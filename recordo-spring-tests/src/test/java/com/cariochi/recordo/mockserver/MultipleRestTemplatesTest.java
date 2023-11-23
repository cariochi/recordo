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
import static com.cariochi.recordo.config.Profiles.SIMPLE;

@SpringBootTest
@ActiveProfiles({REST_TEMPLATE, SIMPLE, APACHE_HTTP, OK_HTTP})
@ExtendWith(RecordoExtension.class)
class MultipleRestTemplatesTest {

    @Autowired
    @EnableRecordo
    private RestTemplate simpleRestTemplate;

    @Autowired
    private RestTemplate apacheRestTemplate;

    @Autowired
    private RestTemplate okHttpRestTemplate;

    @Value("${github.key}")
    private String key;

    @Test
    @MockServer(httpClient = "simpleRestTemplate", value = "/mockserver/multiple-rest-templates/simple.rest.json")
    @MockServer(httpClient = "apacheRestTemplate", value = "/mockserver/multiple-rest-templates/apache.rest.json")
    @MockServer(httpClient = "okHttpRestTemplate", value = "/mockserver/multiple-rest-templates/okHttp.rest.json")
    void should_use_multiple_rest_templates() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(simpleRestTemplate, key);
        simpleGitHubClient.getGists();

        final GitHubRestTemplate apacheGitHubClient = new GitHubRestTemplate(apacheRestTemplate, key);
        apacheGitHubClient.getGists();

        final GitHubRestTemplate okHttpGitHubClient = new GitHubRestTemplate(okHttpRestTemplate, key);
        okHttpGitHubClient.getGists();
    }

    @Test
    @MockServer("/mockserver/multiple-rest-templates/simple.rest.json")
    void should_use_default_rest_template() {

        final GitHubRestTemplate simpleGitHubClient = new GitHubRestTemplate(simpleRestTemplate, key);
        simpleGitHubClient.getGists();
    }

}
