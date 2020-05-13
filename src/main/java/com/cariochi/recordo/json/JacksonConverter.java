package com.cariochi.recordo.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class JacksonConverter implements JsonConverter {

    private final Supplier<ObjectMapper> objectMapper;

    public JacksonConverter() {
        this(() -> new ObjectMapper().registerModule(new JavaTimeModule()).setDateFormat(new StdDateFormat()));
    }

    @SneakyThrows
    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        final JsonNode jsonNode = objectMapper.get().valueToTree(object);
        applyFilter(jsonNode, filter);
        return objectMapper.get().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    @SneakyThrows
    @Override
    public Object fromJson(String json, Type type) {
        return objectMapper.get().readValue(
                json,
                objectMapper.get().getTypeFactory().constructType(type)
        );
    }

    private void applyFilter(JsonNode target, JsonPropertyFilter filter) {
        if (target instanceof ObjectNode) {
            applyFilter((ObjectNode) target, filter);
        } else if (target instanceof ArrayNode) {
            target.forEach(n -> applyFilter(n, filter));
        }
    }

    @SneakyThrows
    private void applyFilter(ObjectNode target, JsonPropertyFilter filter) {
        final Iterator<Map.Entry<String, JsonNode>> fieldIterator = target.fields();
        while (fieldIterator.hasNext()) {
            final Map.Entry<String, JsonNode> field = fieldIterator.next();
            if (filter.shouldExclude(field.getKey())) {
                fieldIterator.remove();
            }
        }
        target.fields().forEachRemaining(field -> {
            final JsonPropertyFilter nextFilter = filter.next(field.getKey());
            if (nextFilter.hasProperties()) {
                applyFilter(field.getValue(), nextFilter);
            }
        });
    }

}
