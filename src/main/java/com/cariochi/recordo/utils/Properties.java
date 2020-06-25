package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.annotation.Resources;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.cariochi.recordo.utils.Reflection.findAnnotation;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class Properties {

    private static final java.util.Properties defaultProperties = loadProperties("/recordo.default.properties");
    private static final java.util.Properties properties = loadProperties("/recordo.properties");

    public static String composeFileName(String fileName, Class<?> testClass) {
        final String folderName =
                findAnnotation(testClass, Resources.class).map(Resources::value).orElse("/");

        return (folderName + "/" + fileName).replace("//", "/");
    }

    public static String resourcesRootFolder() {
        return property("resources.root.folder");
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
