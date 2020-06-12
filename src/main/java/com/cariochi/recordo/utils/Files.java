package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Optional;

import static com.cariochi.recordo.utils.Properties.resourcesFolderPath;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public class Files {

    private static final Logger log = getLogger(Files.class);
    public final String USER_DIR = getProperty("user.dir");

    public String readFromFile(String fileName) throws FileNotFoundException {
        final Optional<File> folder = resourceFolder();
        return folder.isPresent()
                ? readFromFile(new File(folder.get(), fileName))
                : readFromResources(fileName);
    }

    private String readFromResources(String fileName) {
        try (InputStream inputStream = Files.class.getResourceAsStream(fileName)) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }

    private String readFromFile(File file) throws FileNotFoundException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return IOUtils.toString(inputStream, UTF_8);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }

    public Optional<File> writeToFile(String json, String fileName) {
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

    public String filePath(String fileName) {
        return findFile(fileName)
                .map(File::getAbsolutePath)
                .orElse(fileName);
    }

    public Optional<File> findFile(String fileName) {
        return resourceFolder()
                .map(folder -> new File(folder, fileName));
    }

    private Optional<File> resourceFolder() {
        return Optional.of(resourcesFolderPath())
                .map(folder -> new File(USER_DIR, folder))
                .filter(File::exists);
    }


}
