package com.cariochi.recordo.json;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import static com.cariochi.reflecto.Reflecto.reflect;

@UtilityClass
public class JsonConverters {

    public JsonConverter find(Object testInstance) {
        return reflect(testInstance).fields()
                .withTypeAndAnnotation(ObjectMapper.class, EnableRecordo.class).stream().findAny()
                .map(JavaField::getValue)
                .map(ObjectMapper.class::cast)
                .map(JsonConverter::new)
                .orElseGet(JsonConverter::new);
    }

}
