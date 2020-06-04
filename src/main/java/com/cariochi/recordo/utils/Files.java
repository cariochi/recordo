package com.cariochi.recordo.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Optional;

import static com.cariochi.recordo.utils.Format.format;
import static com.cariochi.recordo.utils.Properties.resourcesFolderPath;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public final class Files {

    private static final Logger log = getLogger(Files.class);
    public static final String USER_DIR = getProperty("user.dir");

    private Files() {
    }

    public static String readFromFile(String fileName) throws IOException {
        final Optional<File> folder = resourceFolder();
        return folder.isPresent()
                ? readFromFile(new File(folder.get(), fileName))
                : readFromResources(fileName);
    }

    public static String readFromResources(String fileName) throws IOException {
        try (InputStream inputStream = Files.class.getResourceAsStream(fileName)) {
            return IOUtils.toString(inputStream, UTF_8);
        }
    }

    private static String readFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException(format("\nFile '{}' not found.", file.getAbsolutePath()));
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toString(inputStream, UTF_8);
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

    public static String filePath(String fileName) {
        return findFile(fileName)
                .map(File::getAbsolutePath)
//                .map(path -> "file://" + path)
                .orElse(fileName);
    }

    public static Optional<File> findFile(String fileName) {
        return resourceFolder()
                .map(folder -> new File(folder, fileName));
    }

    private static Optional<File> resourceFolder() {
        return Optional.of(resourcesFolderPath())
                .map(folder -> new File(USER_DIR, folder))
                .filter(File::exists);
    }


}
