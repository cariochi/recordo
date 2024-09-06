package com.cariochi.recordo.core.utils;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static java.lang.Thread.currentThread;

@Slf4j
@UtilityClass
public class Properties {

    private final java.util.Properties defaultProperties = loadProperties("recordo.default.properties");
    private final java.util.Properties properties = loadProperties("recordo.properties");

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
        return properties.getProperty(name, defaultProperties.getProperty(name));
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
