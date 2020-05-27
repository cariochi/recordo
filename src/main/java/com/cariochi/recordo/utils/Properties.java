package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.utils.Fields.ObjectField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public final class Properties {

    private static final java.util.Properties defaultProperties = loadProperties("/recordo.default.properties");
    private static final java.util.Properties properties = loadProperties("/recordo.properties");

    public static final String PACKAGE = "{package}";
    public static final String CLASS = "{class}";
    public static final String METHOD = "{method}";
    public static final String FIELD = "{field}";
    public static final String[] FILE_NAME_VARIABLES = {PACKAGE, CLASS, METHOD, FIELD};

    private Properties() {
    }

    public static String fileName(String fileNamePattern, ObjectField field, Method method) {
        final String packageNme = replace(uncapitalize(field.getObjectClass().getPackage().getName()), ".", "/");
        final String className = uncapitalize(field.getObjectClass().getSimpleName());
        final String methodName = method.getName();
        final String[] values = new String[]{packageNme, className, methodName, field.getName()};
        return replaceEach(fileNamePattern, FILE_NAME_VARIABLES, values);
    }

    public static String fileName(String fileNamePattern, Class<?> testClass, Method method) {
        final String packageNme = replace(uncapitalize(testClass.getPackage().getName()), ".", "/");
        final String className = uncapitalize(testClass.getSimpleName());
        final String methodName = method.getName();
        final String[] values = new String[]{packageNme, className, methodName, ""};
        return replaceEach(fileNamePattern, FILE_NAME_VARIABLES, values);
    }

    public static String resourcesFolderPath() {
        return property("resources.folder");
    }

    public static String givenFileNamePattern() {
        return property("given.filename.pattern");
    }

    public static String givenValueFileNamePattern() {
        return property("given.value.filename.pattern");
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
