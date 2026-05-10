package com.cariochi.recordo.assertions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonFilter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.assertj.core.api.Condition;
import org.skyscreamer.jsonassert.JSONCompareMode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.cariochi.recordo.core.json.JsonUtils.compareMode;

/**
 * AssertJ {@link Condition} that compares an actual object with an expected JSON resource file.
 *
 * @param <T> actual value type
 */
public class JsonCondition<T> extends Condition<T> {

    private final RecordoPredicate<T> predicate;

    /**
     * Creates a condition that checks whether the actual value matches the expected JSON file.
     *
     * @param fileName expected JSON file under the configured Recordo resource root
     * @param <T>      actual value type
     * @return JSON comparison condition
     */
    public static <T> JsonCondition<T> equalAsJsonTo(String fileName) {
        return new JsonCondition<>(new RecordoPredicate<>(fileName));
    }

    private JsonCondition(RecordoPredicate<T> predicate) {
        super(predicate, "");
        this.predicate = predicate;
    }

    /**
     * Uses the supplied ObjectMapper for JSON serialization.
     */
    public JsonCondition<T> using(ObjectMapper objectMapper) {
        predicate.using(objectMapper);
        return this;
    }

    /**
     * Compares only the specified JSON paths.
     */
    public JsonCondition<T> including(String... fields) {
        predicate.including(List.of(fields));
        return this;
    }

    /**
     * Ignores the specified JSON paths during comparison.
     */
    public JsonCondition<T> excluding(String... fields) {
        predicate.excluding(List.of(fields));
        return this;
    }

    /**
     * Sets whether the comparison should allow additional fields in the actual JSON.
     */
    public JsonCondition<T> allowExtraFields(boolean allowExtraFields) {
        predicate.allowExtraFields(allowExtraFields);
        return this;
    }

    /**
     * Sets whether the comparison should allow additional fields in the actual JSON.
     * Defaults to false.
     *
     * @param allowExtraFields True if the actual JSON can have additional fields, false otherwise.
     * @return This JsonCondition instance for method chaining.
     * @deprecated Use {@link #allowExtraFields(boolean)} instead.
     */
    @Deprecated(since = "2.1.0", forRemoval = true)
    public JsonCondition<T> extensible(boolean allowExtraFields) {
        return allowExtraFields(allowExtraFields);
    }

    /**
     * Sets whether array order must match exactly.
     */
    public JsonCondition<T> withStrictOrder(boolean strictOrder) {
        predicate.strictOrder(strictOrder);
        return this;
    }

    @RequiredArgsConstructor
    @Setter
    @Accessors(fluent = true)
    private static class RecordoPredicate<T> implements Predicate<T> {

        private final AsJsonComparator comparator = new AsJsonComparator();
        private final String fileName;

        private List<String> including = new ArrayList<>();
        private List<String> excluding = new ArrayList<>();
        private boolean allowExtraFields = false;
        private boolean strictOrder = true;

        @Override
        public boolean test(T actual) {
            final JsonFilter jsonFilter = new JsonFilter(including, excluding);
            final JSONCompareMode compareMode = compareMode(allowExtraFields, strictOrder);
            return comparator.compareAsJson(actual, fileName, jsonFilter, compareMode).passed();
        }

        public void using(ObjectMapper objectMapper) {
            comparator.setJsonConverter(new JsonConverter(objectMapper));
        }

    }

}
