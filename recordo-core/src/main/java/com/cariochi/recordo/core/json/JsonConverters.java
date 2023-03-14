package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static com.cariochi.recordo.core.utils.BeanUtils.findBean;

@UtilityClass
public class JsonConverters {

    public static Optional<JsonConverter> findJsonConverter(ExtensionContext context) {
        return findBean(ObjectMapper.class, context)
                .map(ObjectMapper.class::cast)
                .map(JsonConverter::new);
    }

    public static JsonConverter getJsonConverter(ExtensionContext context) {
        return findJsonConverter(context)
                .orElseGet(JsonConverter::new);
    }

}
