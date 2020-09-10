package com.cariochi.recordo.given;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;

@Slf4j
@UtilityClass
public class GivenObjectReader {

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public <T> T read(String file, Type parameterType, JsonConverter jsonConverter) {
        try {
            return jsonConverter.fromJson(Files.read(file), parameterType);
        } catch (NoSuchFileException e) {
            return generate(file, parameterType, jsonConverter, e.getMessage());
        }
    }

    private <T> T generate(String file, Type parameterType, JsonConverter jsonConverter, String errorMessage) {
        T givenObject = randomDataGenerator.generateObject(parameterType);
        final String json = givenObject instanceof String ? "{}" : jsonConverter.toJson(givenObject);
        Files.write(json, file)
                .ifPresent(path -> log.warn(errorMessage + "\nRandom value is generated: file://{}", path));
        return givenObject;
    }
}
