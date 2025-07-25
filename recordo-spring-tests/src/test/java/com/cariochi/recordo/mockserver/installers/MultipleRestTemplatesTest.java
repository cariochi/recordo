package com.cariochi.recordo.mockserver.installers;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RecordoExtension;
import com.cariochi.recordo.mockserver.MockServer;
import com.cariochi.recordo.mockserver.installers.configs.FeignOkConfig;
import com.cariochi.recordo.mockserver.installers.configs.MultipleRestTemplatesConfig;
import com.cariochi.recordo.mockserver.resttemplate.GitHubRestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = {MultipleRestTemplatesConfig.class, FeignOkConfig.class})
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
    @MockServer(beanName = "restTemplate", value = "/mockserver/multiple-rest-templates/first.rest.json")
    @MockServer(beanName = "secondRestTemplate", value = "/mockserver/multiple-rest-templates/second.rest.json")
    @MockServer(beanName = "thirdRestTemplate", value = "/mockserver/multiple-rest-templates/third.rest.json")
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
