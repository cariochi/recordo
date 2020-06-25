package com.cariochi.recordo.given;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.Files;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;

import static com.cariochi.recordo.utils.Properties.composeFileName;

@Slf4j
@Builder
public class GivenObject {

    private final Object testInstance;
    private final String file;
    private final Type parameterType;

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    public Object get() {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final String fileName = composeFileName(file, testInstance.getClass());
        Object givenObject;
        try {
            final String json = Files.readFromFile(fileName);
            return String.class.equals(parameterType)
                    ? json
                    : jsonConverter.fromJson(json, parameterType);
        } catch (FileNotFoundException e) {
            givenObject = randomDataGenerator.generateObject(parameterType);
            final String json = givenObject instanceof String ? "{}" : jsonConverter.toJson(givenObject);
            Files.writeToFile(json, fileName)
                    .map(File::getAbsolutePath)
                    .ifPresent(filePath ->
                            log.warn(
                                    e.getMessage() + "\nRandom value is generated.\n\t* {}",
                                    filePath
                            )
                    );
        }
        return givenObject;
    }
}
