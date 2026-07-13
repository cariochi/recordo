package com.cariochi.recordo.core.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;

@UtilityClass
public class Properties {

    private final java.util.Properties defaultProperties = loadProperties("recordo.default.properties");
    private final java.util.Properties fileProperties = loadProperties("recordo.properties");

    public String resourcesRootFolder() {
        return property("resources.root.folder");
    }

    public List<String> httpMocksIncludedHeaders() {
        return Stream.of(property("http.mocks.headers.included").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    public List<String> httpMocksSensitiveHeaders() {
        return Stream.of(property("http.mocks.headers.sensitive").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private String property(String name) {
        // 1. System property: -Drecordo.resources.root.folder=...
        String value = System.getProperty("recordo." + name);
        if (value != null) {
            return value;
        }
        // 2. Spring Environment: recordo.resources.root.folder in application-test.yaml etc.
        value = EnvironmentHolder.get(name);
        if (value != null) {
            return value;
        }
        // 3. recordo.properties on classpath
        value = fileProperties.getProperty(name);
        if (value != null) {
            return value;
        }
        // 4. recordo.default.properties
        return defaultProperties.getProperty(name);
    }

    @SneakyThrows
    public java.util.Properties loadProperties(String fileName) {
        try (InputStream inputStream = currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            java.util.Properties properties = new java.util.Properties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        }
    }

}
