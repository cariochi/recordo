package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.utils.Files;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.csv.CsvSchema.emptySchema;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "assertCsv")
public class CsvAssertion {

    private final String actualCsv;

    @Setter
    private boolean withHeaders;

    @Setter
    private boolean withStrictOrder;

    @Setter
    private char withColumnSeparator = ',';

    @Setter
    private String withLineSeparator = "\n";

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
        final List<Object> expected = read(Files.read(fileName));
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
        final CsvSchema schema = emptySchema()
                .withHeader()
                .withColumnSeparator(withColumnSeparator)
                .withLineSeparator(withLineSeparator);
        return new CsvMapper()
                .readerFor(Map.class)
                .with(schema)
                .readValues(csv)
                .readAll();
    }

    @SneakyThrows
    private List<Object> readWithoutHeaders(String csv) {
        final CsvSchema schema = emptySchema()
                .withoutHeader()
                .withColumnSeparator(withColumnSeparator)
                .withLineSeparator(withLineSeparator);
        return new CsvMapper()
                .enable(CsvParser.Feature.WRAP_AS_ARRAY)
                .readerFor(List.class)
                .with(schema)
                .readValues(csv)
                .readAll();
    }

    private void writeFile(String s) {
        Files.write(actualCsv, s, false)
                .ifPresent(file -> log.info("\nExpected value is saved to file://{}", file));
    }

    private String actualFileName(String expectedFileName) {
        return new StringBuilder(expectedFileName)
                .insert(expectedFileName.lastIndexOf('/') + 1, "ACTUAL/")
                .toString();
    }

}
