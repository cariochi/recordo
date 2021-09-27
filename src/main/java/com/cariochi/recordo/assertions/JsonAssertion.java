package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonPropertyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.ArrayList;
import java.util.List;

import static com.cariochi.recordo.assertions.JsonUtils.compareMode;
import static java.util.Arrays.asList;

@Slf4j
@RequiredArgsConstructor(staticName = "assertAsJson")
public class JsonAssertion<T> {

    private final RecordoJsonComparator<T> jsonComparator = new RecordoJsonComparator<>();
    private final T actual;

    private List<String> including = new ArrayList<>();
    private List<String> excluding = new ArrayList<>();
    private boolean extensible = true;
    private boolean strictOrder = true;

    public JsonAssertion<T> using(ObjectMapper mapper) {
        jsonComparator.setJsonConverter(new JsonConverter(mapper));
        return this;
    }

    public JsonAssertion<T> including(String... fields) {
        this.including = asList(fields);
        return this;
    }

    public JsonAssertion<T> excluding(String... fields) {
        this.excluding = asList(fields);
        return this;
    }

    public JsonAssertion<T> extensible(boolean extensible) {
        this.extensible = extensible;
        return this;
    }

    public JsonAssertion<T> withStrictOrder(boolean strictOrder) {
        this.strictOrder = strictOrder;
        return this;
    }

    public void isEqualTo(String fileName) {
        final JsonPropertyFilter jsonFilter = new JsonPropertyFilter(including, excluding);
        final JSONCompareMode compareMode = compareMode(extensible, strictOrder);
        final JSONCompareResult compareResult = jsonComparator.compareAsJson(actual, fileName, jsonFilter, compareMode);
        if (compareResult.failed()) {
            throw new AssertionError(compareResult.getMessage());
        }
    }
}
