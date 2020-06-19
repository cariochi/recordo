package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.replaceEach;

@UtilityClass
public class Properties {

    private static final java.util.Properties defaultProperties = loadProperties("/recordo.default.properties");
    private static final java.util.Properties properties = loadProperties("/recordo.properties");

    public static final String PACKAGE = "{package}";
    public static final String CLASS = "{class}";
    public static final String METHOD = "{method}";
    public static final String FIELD = "{field}";
    public static final String[] FILE_NAME_VARIABLES = {PACKAGE, CLASS, METHOD, FIELD};

    public static String fileName(String pattern, Class<?> testClass, String method, String field) {
        final String packageNme = Optional.ofNullable(testClass.getPackage())
                .map(Package::getName)
                .map(s -> replace(s, ".", "/"))
                .orElse("");
        final String className = testClass.getSimpleName();
        final String[] values = new String[]{packageNme, className, method, field};
        return replaceEach(pattern, FILE_NAME_VARIABLES, values).replace("//", "/");
    }

    public static String resourcesFolderPath() {
        return property("resources.folder");
    }

    public static String givenFileNamePattern(String pattern) {
        return Optional.ofNullable(pattern)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> property("given.filename.pattern"));
    }

    public static String verifyFileNamePattern(String pattern) {
        return Optional.ofNullable(pattern)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> property("verify.filename.pattern"));
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
