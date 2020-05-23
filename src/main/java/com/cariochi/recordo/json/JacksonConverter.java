package com.cariochi.recordo.json;

import com.cariochi.recordo.RecordoError;
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

public class JacksonConverter implements JsonConverter {

    private final ObjectMapper objectMapper;

    public JacksonConverter() {
        this(new ObjectMapper().registerModule(new JavaTimeModule()).setDateFormat(new StdDateFormat()));
    }

    public JacksonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Object object, JsonPropertyFilter filter) {
        try {
            final JsonNode jsonNode = objectMapper.valueToTree(object);
            applyFilter(jsonNode, filter);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RecordoError(e);
        }
    }

    @Override
    public <T> T fromJson(String json, Type type) {
        try {
            final JavaType valueType = objectMapper.getTypeFactory().constructType(type);
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RecordoError(e);
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
