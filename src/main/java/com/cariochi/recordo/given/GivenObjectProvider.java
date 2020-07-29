package com.cariochi.recordo.given;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.file.NoSuchFileException;

@Slf4j
@Builder
@RequiredArgsConstructor
public class GivenObjectProvider {

    private final JsonConverter jsonConverter;

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public <T> T get(String file, Type parameterType) {
        try {
            return jsonConverter.fromJson(Files.read(file), parameterType);
        } catch (NoSuchFileException e) {
            return generate(file, parameterType, e);
        }
    }

    private <T> T generate(String file, Type parameterType, NoSuchFileException e) {
        T givenObject = randomDataGenerator.generateObject(parameterType);
        final String json = givenObject instanceof String ? "{}" : jsonConverter.toJson(givenObject);
        Files.write(json, file)
                .ifPresent(path -> log.warn(e.getMessage() + "\nRandom value is generated: file://{}", path));
        return givenObject;
    }
}
