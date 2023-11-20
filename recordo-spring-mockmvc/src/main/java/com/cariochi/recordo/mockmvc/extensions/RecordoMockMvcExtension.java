package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.core.utils.Beans.SpringBeans;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.web.servlet.MockMvc;

public class RecordoMockMvcExtension implements Extension, BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        final SpringBeans springBeans = Beans.of(context).springBeans();
        springBeans.findByType(MockMvc.class).ifPresent(mockMvc -> {
            springBeans.findByType(ObjectMapper.class).ifPresentOrElse(
                    objectMapper -> springBeans.register("jsonConverter", JsonConverter.class, objectMapper),
                    () -> springBeans.register("jsonConverter", JsonConverter.class)
            );
            final JsonConverter jsonConverter = springBeans.findByType(JsonConverter.class).orElseThrow();
            springBeans.register("recordoMockMvc", RecordoMockMvc.class, mockMvc, jsonConverter);
        });
    }

}
