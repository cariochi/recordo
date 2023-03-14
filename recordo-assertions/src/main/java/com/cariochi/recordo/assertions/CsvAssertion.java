package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.utils.Files;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.WRAP_AS_ARRAY;
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
        final ObjectReader objectReader = new CsvMapper()
                .enable(WRAP_AS_ARRAY)
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
