package com.cariochi.recordo.json;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

@RequiredArgsConstructor
public class GsonConverter implements JsonConverter {

    private final Gson gson;

    public GsonConverter() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

    @Override
    public String toJson(Object object) {
        if (object == null || object instanceof String) {
            return (String) object;
        }
        return gson.toJson(object);
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        if (object == null || object instanceof String) {
            return (String) object;
        }
        if (filter.hasProperties()) {
            final JsonElement jsonElement = gson.toJsonTree(object);
            applyFilter(jsonElement, filter);
            return toJson(jsonElement);
        } else {
            return toJson(object);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type type) {
        if (json == null) {
            return null;
        }
        if (String.class.equals(type)) {
            return (T) json;
        }
        return gson.fromJson(json, type);
    }

    private void applyFilter(JsonElement target, JsonPropertyFilter filter) {
        if (target instanceof JsonObject) {
            applyFilter((JsonObject) target, filter);
        } else if (target instanceof JsonArray) {
            ((JsonArray) target).forEach(n -> applyFilter(n, filter));
        }
    }

    private void applyFilter(JsonObject target, JsonPropertyFilter filter) {
        target.entrySet().removeIf(f -> filter.shouldExclude(f.getKey()));
        target.entrySet().forEach(property -> {
            final JsonPropertyFilter nextFilter = filter.next(property.getKey());
            if (nextFilter.hasProperties()) {
                final JsonElement newTarget = property.getValue();
                applyFilter(newTarget, nextFilter);
            }
        });
    }

}
