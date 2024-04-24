package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static com.cariochi.recordo.core.json.JsonUtils.compareMode;
import static java.util.Arrays.asList;

/**
 * This class provides assertion methods for comparing objects to JSON files.
 * It utilizes the `org.skyscreamer.jsonassert` library for JSON comparison.
 */
@Slf4j
@RequiredArgsConstructor(staticName = "assertAsJson")
public class JsonAssertion<T> {

    private final AsJsonComparator comparator = new AsJsonComparator();
    private final T actual;

    private List<String> including = new ArrayList<>();
    private List<String> excluding = new ArrayList<>();
    private boolean extensible = false;
    private boolean strictOrder = true;

    /**
     * Sets the ObjectMapper to be used for JSON conversion.
     *
     * @param mapper The ObjectMapper instance used for JSON serialization/deserialization.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> using(ObjectMapper mapper) {
        comparator.setJsonConverter(new JsonConverter(mapper));
        return this;
    }

    /**
     * Specifies a list of fields to be included during comparison. Excluded by default.
     * <p>
     * Fields can be specified with nested structures using dot notation (e.g., "parent.name", "user.role.name"). You can also use index for collections and arrays (e.g., "children[0].id", "issues[0].tags[0].text") or wildcard character "*"
     * to match any element (e.g., "children[*].id", "issues[*].tags[*].text").
     *
     * @param fields The list of field names to include in the comparison.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> including(String... fields) {
        this.including = asList(fields);
        return this;
    }

    /**
     * Specifies a list of fields to be excluded during comparison.
     * <p>
     * Fields can be specified with nested structures using dot notation (e.g., "parent.name", "user.role.name").
     * You can also use index for collections and arrays (e.g., "children[0].id", "issues[0].tags[0].text")
     * or wildcard character "*" to match any element (e.g., "children[*].id", "issues[*].tags[*].text").
     *
     * @param fields The list of field names to exclude from the comparison.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> excluding(String... fields) {
        this.excluding = asList(fields);
        return this;
    }

    /**
     * Sets whether the comparison should allow for additional properties in the expected JSON.
     * Defaults to false.
     *
     * @param extensible True if the expected JSON can have additional properties, false otherwise.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> extensible(boolean extensible) {
        this.extensible = extensible;
        return this;
    }

    /**
     * Sets whether the order of elements in the JSON arrays should be strictly enforced.
     * Defaults to true.
     *
     * @param strictOrder True if the order of elements matters, false otherwise.
     * @return This JsonAssertion instance for method chaining.
     */
    public JsonAssertion<T> withStrictOrder(boolean strictOrder) {
        this.strictOrder = strictOrder;
        return this;
    }

    /**
     * Asserts that the actual object is equal to the content of the specified JSON file.
     *
     * @param fileName The path to the JSON file containing the expected content.
     * @throws AssertionError if the comparison fails.
     */
    public void isEqualTo(String fileName) {
        final JsonFilter jsonFilter = new JsonFilter(including, excluding);
        final JSONCompareMode compareMode = compareMode(extensible, strictOrder);
        final JSONCompareResult compareResult = comparator.compareAsJson(actual, fileName, jsonFilter, compareMode);
        if (compareResult.failed()) {
            throw new AssertionError(compareResult.getMessage());
        }
    }
}
