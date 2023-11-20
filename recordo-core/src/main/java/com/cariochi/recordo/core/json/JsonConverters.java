package com.cariochi.recordo.core.json;

import com.cariochi.recordo.core.utils.Beans;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@UtilityClass
public class JsonConverters {

    public static JsonConverter getJsonConverter(String beanName, ExtensionContext context) {
        final Optional<ObjectMapper> objectMapper = Beans.of(context).find(beanName, ObjectMapper.class);
        if (isNotEmpty(beanName) && objectMapper.isEmpty()) {
            throw new IllegalArgumentException(format("No bean named '%s' available", beanName));
        }
        return objectMapper
                .map(JsonConverter::new)
                .orElseGet(JsonConverter::new);
    }

}
