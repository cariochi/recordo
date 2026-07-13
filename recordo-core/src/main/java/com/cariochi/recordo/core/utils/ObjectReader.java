package com.cariochi.recordo.core.utils;

import com.cariochi.objecto.generators.ObjectoGenerator;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.reflecto.types.ReflectoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;

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
        if (byte[].class.equals(type.actualClass())) {
            return Files.readBytes(file);
        }
        String content = Files.readString(file);
        return Files.isYaml(file)
                ? jsonConverter.fromYaml(content, type.actualType())
                : jsonConverter.fromJson(content, type.actualType());
    }

    private Object generate(String file, ReflectoType type) {
        Object givenObject = null;
        String content;
        boolean yaml = Files.isYaml(file);
        try {
            givenObject = generator.apply(type.actualType());
            if (givenObject == null) {
                content = type.is(Collection.class) || type.isArray() ? "[]" : "{}";
            } else {
                content = yaml ? jsonConverter.toYaml(givenObject) : jsonConverter.toJson(givenObject);
            }
        } catch (Exception e) {
            log.error("Cannot serialize object into {}", yaml ? "YAML" : "JSON", e);
            content = "{}";
        }
        Files.write(content, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty {} is generated: file://{}", yaml ? "yaml" : "json", path));
        return givenObject;
    }

}
