package com.cariochi.recordo.core.utils;

import com.cariochi.objecto.generators.ObjectoGenerator;
import com.cariochi.recordo.core.json.JsonConverter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.reflect.TypeUtils.isArrayType;
import static org.apache.commons.lang3.reflect.TypeUtils.isAssignable;

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

    public Object read(String file, Type parameterType) {
        return read(file, parameterType, UnaryOperator.identity());
    }

    public Object read(String file, Type parameterType, UnaryOperator<String> jsonModifier) {
        return Files.exists(file)
                ? byte[].class.equals(parameterType) ? Files.readBytes(file) : jsonConverter.fromJson(jsonModifier.apply(Files.readString(file)), parameterType)
                : generate(file, parameterType);
    }

    private Object generate(String file, Type parameterType) {
        Object givenObject = null;
        String json;
        try {
            givenObject = generator.apply(parameterType);
            json = givenObject == null
                    ? (isAssignable(Collection.class, parameterType) || isArrayType(parameterType) ? "[]" : "{}")
                    : jsonConverter.toJson(givenObject);
        } catch (Exception e) {
            log.error("Cannot serialize object into JSON", e);
            json = "{}";
        }
        Files.write(json, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty json is generated: file://{}", path));
        return givenObject;
    }

}
