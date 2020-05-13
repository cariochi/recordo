package com.cariochi.recordo.json;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class GsonConverter implements JsonConverter {

    private final Supplier<Gson> gson;

    public GsonConverter() {
        this(new GsonBuilder().setPrettyPrinting()::create);
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        final JsonElement jsonElement = gson.get().toJsonTree(object);
        applyFilter(jsonElement, filter);
        return gson.get().toJson(jsonElement);
    }

    @Override
    public Object fromJson(String json, Type type) {
        return gson.get().fromJson(json, type);
    }

    private void applyFilter(JsonElement target, JsonPropertyFilter filter) {
        if (target instanceof JsonObject) {
            applyFilter((JsonObject) target, filter);
        } else if (target instanceof JsonArray) {
            ((JsonArray) target).forEach(n -> applyFilter(n, filter));
        }
    }

    @SneakyThrows
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
