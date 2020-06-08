package com.cariochi.recordo.given;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.utils.Files;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import static org.slf4j.LoggerFactory.getLogger;

public class GivenFileReader {

    private static final Logger log = getLogger(GivenFileReader.class);
    private final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
    private final Files files = new Files();

    public Object readFromFile(final String fileName,
                               Type parameterType,
                               String parameterName,
                               JsonConverter jsonConverter) {
        Object givenObject;
        try {
            final String json = files.readFromFile(fileName);
            return String.class.equals(parameterType)
                    ? json
                    : jsonConverter.fromJson(json, parameterType);
        } catch (IOException e) {
            givenObject = randomDataGenerator.generateObject(parameterType);
            files.writeToFile(jsonConverter.toJson(givenObject), fileName)
                    .map(File::getAbsolutePath)
                    .ifPresent(file ->
                            log.warn(e.getMessage() + "\nRandom '{}' value file was generated.", parameterName)
                    );
        }
        return givenObject;
    }
}
