package com.cariochi.recordo.json;

import com.cariochi.recordo.annotation.RecordoJsonConverter;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
import com.cariochi.recordo.utils.Reflection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.Optional;


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
        return Fields.of(testInstance)
                .findAny(ObjectMapper.class, RecordoJsonConverter.class)
                .map(TargetField::getValue)
                .map(ObjectMapper.class::cast)
                .map(JacksonConverter::new);
    }

    private static Optional<JsonConverter> gsonConverter(Object testInstance) {
        try {
            Reflection.checkClassLoaded("com.google.gson.Gson");
            return Fields.of(testInstance)
                    .findAny(Gson.class, RecordoJsonConverter.class)
                    .map(TargetField::getValue)
                    .map(Gson.class::cast)
                    .map(GsonConverter::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
