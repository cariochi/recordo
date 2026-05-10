package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonFilter;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static com.cariochi.recordo.core.json.JsonUtils.compareMode;

/**
 * Fluent assertion for comparing an actual object with an expected JSON resource file.
 * <p>
 * Recordo serializes the actual value with Jackson and compares it with the expected file. If the expected
 * file is missing, Recordo writes the actual JSON to that path and fails the assertion so the file can be
 * reviewed and committed.
 *
 * @param <T> actual value type
 */
@Slf4j
public class JsonAssertion<T> {

    private final AsJsonComparator comparator = new AsJsonComparator();
    private final T actual;

    private List<String> including = new ArrayList<>();
    private List<String> excluding = new ArrayList<>();
    private boolean allowExtraFields = false;
    private boolean strictOrder = true;

    private JsonAssertion(T actual) {
        this.actual = actual;
    }

    public static <T> JsonAssertion<T> assertAsJson(T actual) {
        return new JsonAssertion<T>(actual);
    }

    /**
     * Uses the supplied ObjectMapper for JSON serialization.
     *
     * @param objectMapper The ObjectMapper instance used for JSON serialization/deserialization.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> using(ObjectMapper objectMapper) {
        comparator.setJsonConverter(new JsonConverter(objectMapper));
        return this;
    }

    /**
     * Compares only the specified JSON paths.
     * <p>
     * Fields can be specified with nested structures using dot notation (e.g., "parent.name", "user.role.name"). You can also use index for collections and arrays (e.g., "children[0].id", "issues[0].tags[0].text") or wildcard character "*"
     * to match any element (e.g., "children[*].id", "issues[*].tags[*].text").
     *
     * @param fields JSON paths to include in the comparison.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> including(String... fields) {
        this.including = List.of(fields);
        return this;
    }

    /**
     * Ignores the specified JSON paths during comparison.
     * <p>
     * Fields can be specified with nested structures using dot notation (e.g., "parent.name", "user.role.name").
     * You can also use index for collections and arrays (e.g., "children[0].id", "issues[0].tags[0].text")
     * or wildcard character "*" to match any element (e.g., "children[*].id", "issues[*].tags[*].text").
     *
     * @param fields JSON paths to exclude from the comparison.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> excluding(String... fields) {
        this.excluding = List.of(fields);
        return this;
    }

    /**
     * Sets whether the comparison should allow additional fields in the actual JSON.
     * Defaults to false.
     *
     * @param allowExtraFields true if actual JSON may contain fields missing from expected JSON
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> allowExtraFields(boolean allowExtraFields) {
        this.allowExtraFields = allowExtraFields;
        return this;
    }

    /**
     * Sets whether the comparison should allow additional fields in the actual JSON.
     * Defaults to false.
     *
     * @param allowExtraFields true if actual JSON may contain fields missing from expected JSON
     * @return This JsonAssertion instance for method chaining.
     * @deprecated Use {@link #allowExtraFields(boolean)} instead.
     */
    @Deprecated(since = "2.1.0", forRemoval = true)
    public JsonAssertion<T> extensible(boolean allowExtraFields) {
        return allowExtraFields(allowExtraFields);
    }

    /**
     * Sets whether array order must match exactly.
     * Defaults to true.
     *
     * @param strictOrder true if array order matters
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> withStrictOrder(boolean strictOrder) {
        this.strictOrder = strictOrder;
        return this;
    }

    /**
     * Asserts that the actual object matches the specified expected JSON file.
     *
     * @param fileName The path to the JSON file containing the expected content.
     * @throws AssertionError if the comparison fails.
     */
    public void isEqualTo(String fileName) {
        final JsonFilter jsonFilter = new JsonFilter(including, excluding);
        final JSONCompareMode compareMode = compareMode(allowExtraFields, strictOrder);
        final JSONCompareResult compareResult = comparator.compareAsJson(actual, fileName, jsonFilter, compareMode);
        if (compareResult.failed()) {
            throw new AssertionError(compareResult.getMessage());
        }
    }
}
