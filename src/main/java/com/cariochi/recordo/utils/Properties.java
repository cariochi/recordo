package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoException;

import java.io.IOException;
import java.io.InputStream;

public final class Properties {

    public static final String DEFAULT_RESOURCES_FOLDER = "src/test/resources";
    public static final String DEFAULT_GIVEN_FILE_NAME_PATTERN =
            "{TEST_CLASS_FULL_NAME}/{TEST_METHOD_NAME}/given-{TEST_FIELD_NAME}.json";
    public static final String DEFAULT_VERIFY_FILE_NAME_PATTERN =
            "{TEST_CLASS_FULL_NAME}/{TEST_METHOD_NAME}/verify-{TEST_FIELD_NAME}.json";

    private static final java.util.Properties properties = loadProperties();

    private Properties() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static java.util.Properties loadProperties() {
        try (InputStream inputStream = Properties.class.getResourceAsStream("/recordo.properties")) {
            java.util.Properties properties = new java.util.Properties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        } catch (IOException e) {
            throw new RecordoException(e);
        }
    }

    public static String resourcesFolderPath() {
        return properties.getProperty("resources.folder", DEFAULT_RESOURCES_FOLDER);
    }

    public static String givenFileNamePattern() {
        return properties.getProperty("given.filename.pattern", DEFAULT_GIVEN_FILE_NAME_PATTERN);
    }

    public static String verifyFileNamePattern() {
        return properties.getProperty("verify.filename.pattern", DEFAULT_VERIFY_FILE_NAME_PATTERN);
    }

}
