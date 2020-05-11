package com.recordo.utils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
public class Files {

    private final String rootFolder;

    @SneakyThrows
    public Optional<String> readFromFile(String fileName) {
        final String filePath = rootFolder + "/" + fileName;
        try (InputStream inputStream = Files.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException(format("File '%s' not found", filePath));
            }
            return Optional.ofNullable(inputStream).map(is -> readFromStream(inputStream));
        }
    }

    @SneakyThrows
    public Optional<File> writeToFile(String json, String fileName) {
        final File dir = new File("src/test/resources");
        if (!dir.exists()) {
            return Optional.empty();
        }
        final File file = new File(dir, rootFolder + "/" + fileName);
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
