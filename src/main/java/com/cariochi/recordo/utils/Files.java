package com.cariochi.recordo.utils;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.cariochi.recordo.utils.RecordoProperties.resourcesFolder;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;

@Data
public class Files {

    public static final String TEST_CLASS_FILL_NAME = "{TEST_CLASS_FILL_NAME}";
    public static final String TEST_CLASS_SIMPLE_NAME = "{TEST_CLASS_SIMPLE_NAME}";
    public static final String TEST_METHOD_NAME = "{TEST_METHOD_NAME}";
    public static final String TEST_FIELD_NAME = "{TEST_FIELD_NAME}";
    public static final String[] FILE_NAME_VARIABLES = {
            TEST_CLASS_FILL_NAME,
            TEST_CLASS_SIMPLE_NAME,
            TEST_METHOD_NAME,
            TEST_FIELD_NAME
    };

    public String fileName(String fileNamePattern, Method method, String fieldName) {
        final String testClassFullName = replace(uncapitalize(method.getDeclaringClass().getName()), ".", "/");
        final String testClassSimpleName = uncapitalize(method.getDeclaringClass().getSimpleName());
        final String testName = method.getName();
        final String[] values = new String[]{testClassFullName, testClassSimpleName, testName, fieldName};
        return replaceEach(fileNamePattern, FILE_NAME_VARIABLES, values);
    }

    public String readFromFile(String fileName) throws IOException {
        try (InputStream inputStream = Files.class.getResourceAsStream("/" + fileName)) {
            if (inputStream == null) {
                throw new FileNotFoundException(format("File '%s' not found", fileName));
            }
            return readFromStream(inputStream);
        }
    }

    @SneakyThrows
    public Optional<File> writeToFile(String json, String fileName) {
        final File dir = new File(resourcesFolder());
        if (!dir.exists()) {
            return Optional.empty();
        }
        final File file = new File(dir, fileName);
        file.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(file)) {
            IOUtils.write(json + '\n', out, UTF_8);
        }
        return Optional.of(file);
    }

    @SneakyThrows
    private String readFromStream(InputStream inputStream) {
        return IOUtils.toString(inputStream, UTF_8);
    }


}
