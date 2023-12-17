package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.json.JsonConverter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.reflect.TypeUtils.isArrayType;
import static org.apache.commons.lang3.reflect.TypeUtils.isAssignable;

@Slf4j
@RequiredArgsConstructor
public class ObjectReader {

    private static final RandomObjectGenerator RANDOM_OBJECT_GENERATOR = new RandomObjectGenerator();

    private final JsonConverter jsonConverter;

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
            givenObject = RANDOM_OBJECT_GENERATOR.generateInstance(parameterType);
            json = givenObject == null
                    ? (isAssignable(Collection.class, parameterType) || isArrayType(parameterType) ? "[]" : "{}")
                    : jsonConverter.toJson(givenObject);
        } catch (Exception e) {
            json = "{}";
        }
        Files.write(json, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty json is generated: file://{}", path));
        return givenObject;
    }

}
