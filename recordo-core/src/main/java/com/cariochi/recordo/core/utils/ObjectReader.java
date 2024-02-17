package com.cariochi.recordo.core.utils;

import com.cariochi.objecto.generators.ObjectoGenerator;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.reflecto.types.ReflectoType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ObjectReader {

    private final JsonConverter jsonConverter;
    private final Function<Type, Object> generator;
    private final ObjectoGenerator objecto = new ObjectoGenerator();

    public ObjectReader(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
        this.generator = objecto::generate;
    }

    public Object read(String file, ReflectoType type) {
        if (!Files.exists(file)) {
            return generate(file, type);
        }
        return byte[].class.equals(type.actualClass())
                ? Files.readBytes(file)
                : jsonConverter.fromJson(Files.readString(file), type.actualType());
    }

    private Object generate(String file, ReflectoType type) {
        Object givenObject = null;
        String json;
        try {
            givenObject = generator.apply(type.actualType());
            if (givenObject == null) {
                json = type.is(Collection.class) || type.isArray() ? "[]" : "{}";
            } else {
                json = jsonConverter.toJson(givenObject);
            }
        } catch (Exception e) {
            log.error("Cannot serialize object into JSON", e);
            json = "{}";
        }
        Files.write(json, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty json is generated: file://{}", path));
        return givenObject;
    }

}
