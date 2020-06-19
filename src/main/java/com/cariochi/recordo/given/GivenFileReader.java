package com.cariochi.recordo.given;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;

import static com.cariochi.recordo.utils.Properties.fileName;
import static com.cariochi.recordo.utils.Properties.givenFileNamePattern;

@Slf4j
public class GivenFileReader {

    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    private final Files files = new Files();

    public Object readFromFile(Object testInstance,
                               String methodName,
                               String file,
                               Type parameterType,
                               String parameterName) {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final String pattern = givenFileNamePattern(file);
        final String fileName = fileName(pattern, testInstance.getClass(), methodName, parameterName);
        Object givenObject;
        try {
            final String json = files.readFromFile(fileName);
            return String.class.equals(parameterType)
                    ? json
                    : jsonConverter.fromJson(json, parameterType);
        } catch (FileNotFoundException e) {
            givenObject = randomDataGenerator.generateObject(parameterType);
            final String json = givenObject instanceof String ? "{}" : jsonConverter.toJson(givenObject);
            files.writeToFile(json, fileName)
                    .map(File::getAbsolutePath)
                    .ifPresent(f ->
                            log.warn(
                                    e.getMessage() + "\nRandom '{}' value is generated.\n\t* {}",
                                    parameterName,
                                    f
                            )
                    );
        }
        return givenObject;
    }
}
