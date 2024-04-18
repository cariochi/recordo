package com.cariochi.recordo.core.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class Files {

    public final Path USER_DIR = Paths.get(getProperty("user.dir"));

    public boolean exists(String file) {
        return java.nio.file.Files.exists(path(file));
    }

    @SneakyThrows
    public String readString(String file) {
        return java.nio.file.Files.readString(path(file), UTF_8);
    }

    @SneakyThrows
    public byte[] readBytes(String file) {
        return java.nio.file.Files.readAllBytes(path(file));
    }

    public Optional<Path> write(String content, String file) {
        return write(content, file, true);
    }

    @SneakyThrows
    public Optional<Path> write(String content, String file, boolean addNewLine) {
        if (!java.nio.file.Files.exists(USER_DIR)) {
            return Optional.empty();
        }
        final Path path = path(file);
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.write(path, (content + (addNewLine ? '\n' : "")).getBytes(UTF_8));
        return Optional.of(path);
    }

    @SneakyThrows
    public Path path(String file) {
        return java.nio.file.Files.exists(USER_DIR)
                ? Paths.get(resourceRootFolder().toString(), file).toAbsolutePath()
                : Paths.get(ClassLoader.getSystemResource(file).toURI());
    }

    @SneakyThrows
    public List<Path> getFileList(String folder) {
        return getFileList(path(folder)).stream()
                .map(Path::getFileName)
                .map(filename -> Path.of(folder, filename.toString()))
                .collect(toList());
    }

    public void delete(String path) {
        if (java.nio.file.Files.exists(USER_DIR)) {
            delete(path(path));
        }
    }

    @SneakyThrows
    private List<Path> getFileList(Path path) {
        try (final Stream<Path> stream = java.nio.file.Files.list(path)) {
            return stream
                    .sorted()
                    .collect(toList());
        }
    }

    @SneakyThrows
    private void delete(Path path) {
        if (java.nio.file.Files.isDirectory(path)) {
            getFileList(path).forEach(Files::delete);
        }
        java.nio.file.Files.delete(path);
    }

    private Path resourceRootFolder() {
        return Paths.get(USER_DIR.toString(), Properties.resourcesRootFolder());
    }

    public static boolean isJson(String path) {
        return path.endsWith(".json");
    }
}
