package com.cariochi.recordo.json;

import com.cariochi.recordo.annotation.RecordoJsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Optional;

import static com.cariochi.recordo.utils.Reflection.readAnnotatedValue;

public interface JsonConverter {

    String toJson(Object object, JsonPropertyFilter filter);

    <T> T fromJson(String json, Type type);

    static JsonConverter of(Object testInstance) {
        return jacksonConverter(testInstance)
                .map(Optional::of)
                .orElseGet(() -> gsonConverter(testInstance))
                .orElseGet(JacksonConverter::new);
    }

    static Optional<JsonConverter> jacksonConverter(Object testInstance) {
        return readAnnotatedValue(testInstance, ObjectMapper.class, RecordoJsonConverter.class)
                .map(JacksonConverter::new);
    }

    static Optional<JsonConverter> gsonConverter(Object testInstance) {
        return readAnnotatedValue(testInstance, Gson.class, RecordoJsonConverter.class)
                .map(GsonConverter::new);
    }
}
