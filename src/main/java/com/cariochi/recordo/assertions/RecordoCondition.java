package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.assertj.core.api.Condition;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.cariochi.recordo.assertions.JsonUtils.compareMode;
import static java.util.Arrays.asList;

public class RecordoCondition<T> extends Condition<T> {

    private final RecordoPredicate<T> predicate;

    public static <T> RecordoCondition<T> equalAsJsonTo(String fileName) {
        return new RecordoCondition<>(new RecordoPredicate<>(fileName));
    }

    private RecordoCondition(RecordoPredicate<T> predicate) {
        super(predicate, "");
        this.predicate = predicate;
    }

    public RecordoCondition<T> using(ObjectMapper mapper) {
        predicate.using(mapper);
        return this;
    }

    public RecordoCondition<T> including(String... fields) {
        predicate.including(asList(fields));
        return this;
    }

    public RecordoCondition<T> excluding(String... fields) {
        predicate.excluding(asList(fields));
        return this;
    }

    public RecordoCondition<T> extensible(boolean extensible) {
        predicate.extensible(extensible);
        return this;
    }

    public RecordoCondition<T> withStrictOrder(boolean strictOrder) {
        predicate.strictOrder(strictOrder);
        return this;
    }

    @RequiredArgsConstructor
    @Setter
    @Accessors(fluent = true)
    private static class RecordoPredicate<T> implements Predicate<T> {

        private final RecordoJsonComparator<T> jsonComparator = new RecordoJsonComparator<>();
        private final String fileName;

        private List<String> including = new ArrayList<>();
        private List<String> excluding = new ArrayList<>();
        private boolean extensible = true;
        private boolean strictOrder = true;

        @Override
        public boolean test(T actual) {
            final JsonPropertyFilter jsonFilter = new JsonPropertyFilter(including, excluding);
            final JSONCompareMode compareMode = compareMode(extensible, strictOrder);
            return jsonComparator.compareAsJson(actual, fileName, jsonFilter, compareMode).passed();
        }

        public void using(ObjectMapper objectMapper) {
            jsonComparator.setJsonConverter(new JacksonConverter(objectMapper));
        }

    }

}
