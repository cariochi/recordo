package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.RandomObjectGenerator;
import com.cariochi.recordo.core.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.UnaryOperator;

import static com.cariochi.recordo.core.utils.Files.readBytes;
import static com.cariochi.recordo.core.utils.Files.readString;
import static java.util.function.UnaryOperator.identity;
import static org.apache.commons.lang3.reflect.TypeUtils.isArrayType;
import static org.apache.commons.lang3.reflect.TypeUtils.isAssignable;

@Slf4j
@RequiredArgsConstructor
public class ObjectReader {

    private final RandomObjectGenerator EMPTY_INSTANCE_GENERATOR = new RandomObjectGenerator();

    private final JsonConverter jsonConverter;

    public Object read(String file, Type parameterType) {
        return read(file, parameterType, identity());
    }

    public Object read(String file, Type parameterType, UnaryOperator<String> jsonModifier) {
        return Files.exists(file)
                ? byte[].class.equals(parameterType) ? readBytes(file) : jsonConverter.fromJson(jsonModifier.apply(readString(file)), parameterType)
                : generate(file, parameterType);
    }

    private Object generate(String file, Type parameterType) {
        Object givenObject = null;
        String json;
        try {
            givenObject = EMPTY_INSTANCE_GENERATOR.generateInstance(parameterType, 2);
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
