package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class Properties {

    private final java.util.Properties defaultProperties = loadProperties("/recordo.default.properties");
    private final java.util.Properties properties = loadProperties("/recordo.properties");

    public String resourcesRootFolder() {
        return property("resources.root.folder");
    }

    public List<String> httpMocksIncludedHeaders() {
        return stream(property("http.mocks.headers.included").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
    }

    public List<String> httpMocksSensitiveHeaders() {
        return stream(property("http.mocks.headers.sensitive").split(","))
                .map(StringUtils::trim)
                .map(StringUtils::lowerCase)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
    }

    private String property(String name) {
        return properties.getProperty(name, defaultProperties.getProperty(name));
    }

    private java.util.Properties loadProperties(String fileName) {
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
