package com.cariochi.recordo.given;

import com.cariochi.recordo.generator.EmptyInstanceGenerator;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;

@Slf4j
@UtilityClass
public class GivenObjectReader {

    private final EmptyInstanceGenerator EMPTY_INSTANCE_GENERATOR = new EmptyInstanceGenerator();

    public Object read(String file, Type parameterType, JsonConverter jsonConverter) {
        try {
            return jsonConverter.fromJson(Files.read(file), parameterType);
        } catch (NoSuchFileException e) {
            return generate(file, parameterType, jsonConverter, e.getMessage());
        }
    }

    private Object generate(String file, Type parameterType, JsonConverter jsonConverter, String errorMessage) {
        Object givenObject = EMPTY_INSTANCE_GENERATOR.createInstance(parameterType, 3);
        final String json = givenObject == null ? "{}" : jsonConverter.toJson(givenObject);
        Files.write(json, file)
                .ifPresent(path -> log.warn(errorMessage + "\nEmpty json is generated: file://{}", path));
        return givenObject;
    }
}
