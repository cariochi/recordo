package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class Properties {

    private static final java.util.Properties defaultProperties = loadProperties("/recordo.default.properties");
    private static final java.util.Properties properties = loadProperties("/recordo.properties");

    private Properties() {
    }

    public static String resourcesFolderPath() {
        return property("resources.folder");
    }

    public static String givenFileNamePattern() {
        return property("given.filename.pattern");
    }

    public static String verifyFileNamePattern() {
        return property("verify.filename.pattern");
    }

    public static String httpMocksFileNamePattern() {
        return property("http.mocks.filename.pattern");
    }

    public static List<String> httpMocksIncludedHeaders() {
        return stream(property("http.mocks.headers.included").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
    }

    public static List<String> httpMocksSensitiveHeaders() {
        return stream(property("http.mocks.headers.sensitive").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
    }

    private static String property(String name) {
        return properties.getProperty(name, defaultProperties.getProperty(name));
    }

    private static java.util.Properties loadProperties(String fileName) {
        try (InputStream inputStream = Properties.class.getResourceAsStream(fileName)) {
            java.util.Properties properties = new java.util.Properties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }
}
