package com.cariochi.recordo.read;

import com.cariochi.recordo.generator.EmptyInstanceGenerator;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
@UtilityClass
public class ObjectReader {

    private final EmptyInstanceGenerator EMPTY_INSTANCE_GENERATOR = new EmptyInstanceGenerator();

    public Object read(String file, Type parameterType, JsonConverter jsonConverter) {
        return Files.exists(file)
                ? jsonConverter.fromJson(Files.read(file), parameterType)
                : generate(file, parameterType, jsonConverter);

    }

    private Object generate(String file, Type parameterType, JsonConverter jsonConverter) {
        Object givenObject = EMPTY_INSTANCE_GENERATOR.createInstance(parameterType, 3);
        final String json = givenObject == null ? "{}" : jsonConverter.toJson(givenObject);
        Files.write(json, file)
                .ifPresent(path -> log.warn("\nFile not found. Empty json is generated: file://{}", path));
        return givenObject;
    }
}
