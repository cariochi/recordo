package com.cariochi.recordo.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.util.Properties;

@UtilityClass
public final class RecordoProperties {

    public static final String DEFAULT_RESOURCES_FOLDER = "src/test/resources";
    public static final String DEFAULT_FILE_NAME_PATTERN =
            "{TEST_CLASS_FILL_NAME}/{TEST_METHOD_NAME}/{TEST_FIELD_NAME}.json";

    private static final Properties properties = loadProperties();

    @SneakyThrows
    private static Properties loadProperties() {
        try (InputStream inputStream = Files.class.getResourceAsStream("/recordo.properties")) {
            Properties properties = new Properties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        }
    }

    public static String resourcesFolder() {
        return properties.getProperty("resources.folder", DEFAULT_RESOURCES_FOLDER);
    }

    public static String givenFileNamePattern() {
        return properties.getProperty("given.filename.pattern", DEFAULT_FILE_NAME_PATTERN);
    }

    public static String verifyFileNamePattern() {
        return properties.getProperty("verify.filename.pattern", DEFAULT_FILE_NAME_PATTERN);
    }

}
