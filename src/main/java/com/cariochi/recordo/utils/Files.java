package com.cariochi.recordo.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Properties.resourcesFolderPath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public final class Files {

    private static final Logger log = getLogger(Files.class);

    public static final String TEST_CLASS_FULL_NAME = "{TEST_CLASS_FULL_NAME}";
    public static final String TEST_CLASS_SIMPLE_NAME = "{TEST_CLASS_SIMPLE_NAME}";
    public static final String TEST_METHOD_NAME = "{TEST_METHOD_NAME}";
    public static final String TEST_FIELD_NAME = "{TEST_FIELD_NAME}";
    public static final String[] FILE_NAME_VARIABLES = {
            TEST_CLASS_FULL_NAME,
            TEST_CLASS_SIMPLE_NAME,
            TEST_METHOD_NAME,
            TEST_FIELD_NAME
    };

    private Files() {
    }

    public static String fileName(String fileNamePattern, Class<?> testClass, Method method, String fieldName) {
        final String testClassFullName = replace(uncapitalize(testClass.getName()), ".", "/");
        final String testClassSimpleName = uncapitalize(testClass.getSimpleName());
        final String testName = method.getName();
        final String[] values = new String[]{testClassFullName, testClassSimpleName, testName, fieldName};
        return replaceEach(fileNamePattern, FILE_NAME_VARIABLES, values);
    }

    public static String readFromFile(String fileName) throws IOException {
        try (InputStream inputStream = Files.class.getResourceAsStream("/" + fileName)) {
            if (inputStream == null) {
                final String filePath = findFile(fileName)
                        .map(File::getAbsolutePath)
                        .map(path -> "file://" + path)
                        .orElse(fileName);
                throw new IOException(format("\nFile '{}' not found.", filePath));
            }
            return readFromStream(inputStream);
        }
    }

    public static Optional<File> writeToFile(String json, String fileName) {
        final Optional<File> fileOptional = findFile(fileName);
        if (fileOptional.isPresent()) {
            try {
                final File file = fileOptional.get();
                file.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(file)) {
                    IOUtils.write(json + '\n', out, UTF_8);
                }
            } catch (IOException e) {
                log.error("Cannot write file", e);
            }
        }
        return fileOptional;
    }

    public static Optional<File> findFile(String fileName) {
        final File resourcesFolder = new File(resourcesFolderPath());
        if (!resourcesFolder.exists()) {
            return Optional.empty();
        }
        return Optional.of(new File(resourcesFolder, fileName));
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream, UTF_8);
    }


}
