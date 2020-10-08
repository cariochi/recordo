package com.cariochi.recordo.assertions;

import com.cariochi.recordo.json.JacksonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Condition;

import static java.util.Arrays.asList;

public class RecordoCondition<T> extends Condition<T> {

    private final RecordoJsonCompare<T> jsomCompare;

    public static <T> RecordoCondition<T> equalAsJsonTo(String fileName) {
        return new RecordoCondition<>(new RecordoJsonCompare<>(fileName));
    }

    private RecordoCondition(RecordoJsonCompare<T> jsonCompare) {
        super(
                actual -> {
                    jsonCompare.setActual(actual);
                    return jsonCompare.compareJson().passed();
                },
                ""
        );
        this.jsomCompare = jsonCompare;
    }

    public RecordoCondition<T> using(ObjectMapper mapper) {
        jsomCompare.setJsonConverter(new JacksonConverter(mapper));
        return this;
    }

    public RecordoCondition<T> including(String... fields) {
        jsomCompare.getIncluded().clear();
        jsomCompare.getIncluded().addAll(asList(fields));
        return this;
    }

    public RecordoCondition<T> excluding(String... fields) {
        jsomCompare.getExcluded().clear();
        jsomCompare.getExcluded().addAll(asList(fields));
        return this;
    }

    public RecordoCondition<T> extensible(boolean extensible) {
        jsomCompare.setExtensible(extensible);
        return this;
    }

    public RecordoCondition<T> withStrictOrder(boolean strictOrder) {
        jsomCompare.setStrictOrder(strictOrder);
        return this;
    }
}
