package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.utils.Files;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.dataformat.csv.CsvReadFeature.WRAP_AS_ARRAY;
import static tools.jackson.dataformat.csv.CsvSchema.emptySchema;

/**
 * Fluent assertion for comparing an actual CSV string with an expected CSV resource file.
 * <p>
 * If the expected file is missing, Recordo writes the actual CSV to that path and fails the assertion so the
 * file can be reviewed and committed. On comparison failure, the actual value is written under an
 * {@code ACTUAL/} folder next to the expected file.
 */
@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "assertCsv")
public class CsvAssertion {

    private final String actualCsv;

    /**
     * Compares rows as objects keyed by the header row.
     */
    @Setter
    private boolean withHeaders;

    /**
     * Requires rows to appear in the same order as the expected file.
     */
    @Setter
    private boolean withStrictOrder;

    /**
     * Configures the CSV column separator. Defaults to comma.
     */
    @Setter
    private char withColumnSeparator = ',';

    /**
     * Configures the CSV line separator. Defaults to {@code \n}.
     */
    @Setter
    private String withLineSeparator = "\n";

    /**
     * Asserts that the actual CSV equals the expected CSV file.
     *
     * @param fileName expected CSV file under the configured Recordo resource root
     */
    public void isEqualsTo(String fileName) {
        if (Files.exists(fileName)) {
            try {
                doAssert(fileName);
            } catch (AssertionError e) {
                writeFile(actualFileName(fileName));
                throw e;
            }
        } else {
            writeFile(fileName);
            throw new AssertionError("Expected CSV file not found");
        }
    }

    private void doAssert(String fileName) {
        final List<Object> actual = read(actualCsv);
        final List<Object> expected = read(Files.readString(fileName));
        if (withStrictOrder) {
            assertThat(actual).containsExactlyElementsOf(expected);
        } else {
            assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    private List<Object> read(String csv) {
        return withHeaders ? readWithHeaders(csv) : readWithoutHeaders(csv);
    }

    @SneakyThrows
    private List<Object> readWithHeaders(String csv) {
        final ObjectReader objectReader = new CsvMapper()
                .readerFor(Map.class)
                .with(schema().withHeader());
        try (final MappingIterator<Object> iterator = objectReader.readValues(csv)) {
            return iterator.readAll();
        }
    }

    @SneakyThrows
    private List<Object> readWithoutHeaders(String csv) {
        final ObjectReader objectReader = CsvMapper.builder()
                .enable(WRAP_AS_ARRAY)
                .build()
                .readerFor(List.class)
                .with(schema().withoutHeader());
        try (final MappingIterator<Object> iterator = objectReader.readValues(csv)) {
            return iterator.readAll();
        }
    }

    private CsvSchema schema() {
        return emptySchema()
                .withColumnSeparator(withColumnSeparator)
                .withLineSeparator(withLineSeparator);
    }

    private void writeFile(String s) {
        Files.write(actualCsv, s, false)
                .ifPresent(file -> log.info("\nExpected value has been saved to file://{}", file));
    }

    private String actualFileName(String expectedFileName) {
        return new StringBuilder(expectedFileName)
                .insert(expectedFileName.lastIndexOf('/') + 1, "ACTUAL/")
                .toString();
    }

}
