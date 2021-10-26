package com.cariochi.recordo.core.json;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;

@UtilityClass
public class JsonConverters {

    public static Optional<JsonConverter> findJsonConverter(Object testInstance) {
        return reflect(testInstance).fields()
                .withTypeAndAnnotation(ObjectMapper.class, EnableRecordo.class).stream().findAny()
                .map(JavaField::getValue)
                .map(ObjectMapper.class::cast)
                .map(JsonConverter::new);
    }

    public static JsonConverter getJsonConverter(Object testInstance) {
        return findJsonConverter(testInstance)
                .orElseGet(JsonConverter::new);
    }

}
