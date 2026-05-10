package com.cariochi.recordo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.SpringDataJackson3Configuration.PageModule;
import org.springframework.data.web.config.SpringDataWebSettings;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.util.StdDateFormat;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.DIRECT;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .defaultDateFormat(new StdDateFormat())
                .addModule(new PageModule(new SpringDataWebSettings(DIRECT)))
                .build();
    }

}
