package com.cariochi.recordo.json;

import com.cariochi.recordo.annotation.RecordoJsonConverter;
import com.cariochi.recordo.utils.Reflection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.Optional;

import static com.cariochi.recordo.utils.Fields.readAnnotatedValue;

public final class JsonConverters {

    private JsonConverters() {
    }

    public static JsonConverter find(Object testInstance) {
        return jacksonConverter(testInstance)
                .map(Optional::of)
                .orElseGet(() -> gsonConverter(testInstance))
                .orElseGet(JacksonConverter::new);
    }

    private static Optional<JsonConverter> jacksonConverter(Object testInstance) {
        return readAnnotatedValue(testInstance, ObjectMapper.class, RecordoJsonConverter.class)
                .map(JacksonConverter::new);
    }

    private static Optional<JsonConverter> gsonConverter(Object testInstance) {
        try {
            Reflection.checkClassLoaded("com.google.gson.Gson");
            return readAnnotatedValue(testInstance, Gson.class, RecordoJsonConverter.class)
                    .map(GsonConverter::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
