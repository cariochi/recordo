package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.IdentityHashMap;

public class MaxDepthCyclicObjectSerializer extends StdSerializer<Object> implements ContextualSerializer, ResolvableSerializer, JsonFormatVisitable {

    private static final int MAX_DEPTH = 2;
    private final BeanSerializer defaultSerializer;
    private final IdentityHashMap<Object, Integer> seenObjects = new IdentityHashMap<>();

    protected MaxDepthCyclicObjectSerializer(BeanSerializer defaultSerializer) {
        super(Object.class);
        this.defaultSerializer = defaultSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int depth = seenObjects.getOrDefault(value, 0);
        if (depth >= MAX_DEPTH) {
            gen.writeNull();
            return;
        }
        seenObjects.put(value, depth + 1);
        defaultSerializer.serialize(value, gen, provider);
        seenObjects.put(value, depth);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        defaultSerializer.resolve(provider);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        return defaultSerializer.createContextual(prov, property);
    }

}
