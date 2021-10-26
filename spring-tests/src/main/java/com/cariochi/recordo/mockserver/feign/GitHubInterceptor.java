package com.cariochi.recordo.mockserver.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitHubInterceptor implements RequestInterceptor {

    @Value("${github.key}")
    public String key;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("Authorization", "Bearer " + key);
    }

}
