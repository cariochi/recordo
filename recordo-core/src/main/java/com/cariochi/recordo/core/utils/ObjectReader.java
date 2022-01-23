package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EmptyInstanceGenerator;
import com.cariochi.recordo.core.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.function.UnaryOperator;

@Slf4j
@RequiredArgsConstructor
public class ObjectReader {

    private final EmptyInstanceGenerator EMPTY_INSTANCE_GENERATOR = new EmptyInstanceGenerator();

    private final JsonConverter jsonConverter;

    public Object read(String file, Type parameterType) {
        return read(file, parameterType, UnaryOperator.identity());
    }

    public Object read(String file, Type parameterType, UnaryOperator<String> jsonModifier) {
        return Files.exists(file)
                ? jsonConverter.fromJson(jsonModifier.apply(Files.read(file)), parameterType)
                : generate(file, parameterType);
    }

    private Object generate(String file, Type parameterType) {
        Object givenObject = EMPTY_INSTANCE_GENERATOR.createInstance(parameterType, 3);
        final String json = givenObject == null ? "{}" : jsonConverter.toJson(givenObject);
        Files.write(json, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty json is generated: file://{}", path));
        return givenObject;
    }

}
