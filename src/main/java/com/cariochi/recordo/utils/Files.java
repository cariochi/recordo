package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
public class Files {

    public final Path USER_DIR = Paths.get(getProperty("user.dir"));

    public String read(String file) throws NoSuchFileException {
        try {
            return new String(java.nio.file.Files.readAllBytes(path(file)), UTF_8);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }

    public Optional<Path> write(String json, String file) {
        if (!java.nio.file.Files.exists(USER_DIR)) {
            return Optional.empty();
        }
        final Path path = path(file);
        try {
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, (json + '\n').getBytes(UTF_8));
        } catch (IOException e) {
            throw new RecordoError(e);
        }
        return Optional.of(path);
    }

    @SneakyThrows
    public Path path(String file) {
        return java.nio.file.Files.exists(USER_DIR)
                ? Paths.get(resourceRootFolder().toString(), file).toAbsolutePath()
                : Paths.get(ClassLoader.getSystemResource(file).toURI());
    }

    private Path resourceRootFolder() {
        return Paths.get(USER_DIR.toString(), Properties.resourcesRootFolder());
    }

}
