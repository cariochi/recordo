package com.cariochi.recordo.assertions;

import org.junit.jupiter.api.Test;

import static com.cariochi.recordo.assertions.CsvAssertion.assertCsv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvAssertionTest {

    private static final String ACTUAL_CSV = "id,text\n1,one\n2,two";

    @Test
    void ok_with_headers_without_strict_order() {
        assertCsv(ACTUAL_CSV)
                .withHeaders(true)
                .withStrictOrder(false)
                .isEqualsTo("/csv/ordered.csv");
    }

    @Test
    void ok_with_headers_with_strict_order() {
        assertCsv(ACTUAL_CSV)
                .withHeaders(true)
                .withStrictOrder(false)
                .isEqualsTo("/csv/reordered_rows.csv");
    }

    @Test
    void ok_with_headers_reordered_columns() {
        assertCsv(ACTUAL_CSV)
                .withHeaders(true)
                .withStrictOrder(true)
                .isEqualsTo("/csv/reordered_columns.csv");
    }

    @Test
    void error_with_headers_with_strict_order() {
        assertThatThrownBy(() ->
                assertCsv(ACTUAL_CSV)
                        .withHeaders(true)
                        .withStrictOrder(true)
                        .isEqualsTo("/csv/reordered_rows.csv")
        )
                .isInstanceOf(AssertionError.class);
    }

    @Test
    void ok_without_headers_with_strict_order() {
        assertCsv(ACTUAL_CSV)
                .withHeaders(false)
                .withStrictOrder(true)
                .isEqualsTo("/csv/ordered.csv");
    }

    @Test
    void ok_without_headers_without_strict_order() {
        assertCsv(ACTUAL_CSV)
                .withHeaders(false)
                .withStrictOrder(false)
                .isEqualsTo("/csv/reordered_rows.csv");
    }

    @Test
    void error_without_headers_reordered_columns() {
        assertThatThrownBy(() ->
                assertCsv(ACTUAL_CSV)
                        .withHeaders(false)
                        .withStrictOrder(true)
                        .isEqualsTo("/csv/reordered_columns.csv")
        ).isInstanceOf(AssertionError.class);
    }

    @Test
    void error_without_headers_with_strict_order() {
        assertThatThrownBy(() ->
                assertCsv(ACTUAL_CSV)
                        .withHeaders(false)
                        .withStrictOrder(true)
                        .isEqualsTo("/csv/reordered_rows.csv")
        ).isInstanceOf(AssertionError.class);
    }

}
