package com.cariochi.recordo.json;

import com.cariochi.recordo.RecordoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public class JacksonConverter implements JsonConverter {

    private final Supplier<ObjectMapper> objectMapper;

    public JacksonConverter() {
        this(() -> new ObjectMapper().registerModule(new JavaTimeModule()).setDateFormat(new StdDateFormat()));
    }

    public JacksonConverter(Supplier<ObjectMapper> objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        try {
            final JsonNode jsonNode = objectMapper.get().valueToTree(object);
            applyFilter(jsonNode, filter);
            return objectMapper.get().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RecordoException(e);
        }
    }

    @Override
    public Object fromJson(String json, Type type) {
        try {
            final JavaType valueType = objectMapper.get().getTypeFactory().constructType(type);
            return objectMapper.get().readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RecordoException(e);
        }
    }

    private void applyFilter(JsonNode target, JsonPropertyFilter filter) {
        if (target instanceof ObjectNode) {
            applyFilter((ObjectNode) target, filter);
        } else if (target instanceof ArrayNode) {
            target.forEach(n -> applyFilter(n, filter));
        }
    }

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
