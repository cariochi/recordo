package com.cariochi.recordo.json;

import com.google.gson.*;

import java.lang.reflect.Type;

public class GsonConverter implements JsonConverter {

    private final Gson gson;

    public GsonConverter() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

    public GsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        final JsonElement jsonElement = gson.toJsonTree(object);
        applyFilter(jsonElement, filter);
        return gson.toJson(jsonElement);
    }

    @Override
    public <T> T fromJson(String json, Type type) {
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
