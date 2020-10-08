package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JacksonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.skyscreamer.jsonassert.JSONCompareResult;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class RecordoAssertion<T> {

    private final RecordoJsonCompare<T> jsonCompare;

    public static <T> RecordoAssertion<T> assertAsJson(T actual) {
        return new RecordoAssertion<>(new RecordoJsonCompare<>(actual));
    }

    public RecordoAssertion<T> using(ObjectMapper mapper) {
        jsonCompare.setJsonConverter(new JacksonConverter(mapper));
        return this;
    }

    public RecordoAssertion<T> including(String... fields) {
        jsonCompare.getIncluded().clear();
        jsonCompare.getIncluded().addAll(asList(fields));
        return this;
    }

    public RecordoAssertion<T> excluding(String... fields) {
        jsonCompare.getExcluded().clear();
        jsonCompare.getExcluded().addAll(asList(fields));
        return this;
    }

    public RecordoAssertion<T> extensible(boolean extensible) {
        jsonCompare.setExtensible(extensible);
        return this;
    }

    public RecordoAssertion<T> withStrictOrder(boolean strictOrder) {
        jsonCompare.setStrictOrder(strictOrder);
        return this;
    }

    public void isEqualTo(String fileName) {
        jsonCompare.setFileName(fileName);
        final JSONCompareResult compareResult = jsonCompare.compareJson();
        if (compareResult.failed()) {
            throw new AssertionError(compareResult.getMessage());
        }
    }
}
