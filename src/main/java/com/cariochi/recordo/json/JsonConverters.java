package com.cariochi.recordo.json;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.utils.reflection.ClassLoaders;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class JsonConverters {

    public  JsonConverter find(Object testInstance) {
        return jacksonConverter(testInstance)
                .map(Optional::of)
                .orElseGet(() -> gsonConverter(testInstance))
                .orElseGet(JacksonConverter::new);
    }

    private  Optional<JsonConverter> jacksonConverter(Object testInstance) {
        return Fields.of(testInstance)
                .withTypeAndAnnotation(ObjectMapper.class, EnableRecordo.class).stream().findAny()
                .map(TargetField::getValue)
                .map(ObjectMapper.class::cast)
                .map(JacksonConverter::new);
    }

    private  Optional<JsonConverter> gsonConverter(Object testInstance) {
        try {
            ClassLoaders.checkClassLoaded("com.google.gson.Gson");
            return Fields.of(testInstance)
                    .withTypeAndAnnotation(Gson.class, EnableRecordo.class).stream().findAny()
                    .map(TargetField::getValue)
                    .map(Gson.class::cast)
                    .map(GsonConverter::new);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
