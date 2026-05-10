package com.cariochi.recordo.core.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import tools.jackson.databind.ser.BeanSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.IdentityHashMap;

public class MaxDepthCyclicObjectSerializer extends StdSerializer<Object> implements JsonFormatVisitable {

    private static final int MAX_DEPTH = 2;
    private final BeanSerializer defaultSerializer;
    private final IdentityHashMap<Object, Integer> seenObjects = new IdentityHashMap<>();

    protected MaxDepthCyclicObjectSerializer(BeanSerializer defaultSerializer) {
        super(Object.class);
        this.defaultSerializer = defaultSerializer;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        int depth = seenObjects.getOrDefault(value, 0);
        if (depth >= MAX_DEPTH) {
            gen.writeNull();
            return;
        }
        seenObjects.put(value, depth + 1);
        defaultSerializer.serialize(value, gen, ctxt);
        seenObjects.put(value, depth);
    }

    @Override
    public void resolve(SerializationContext ctxt) {
        defaultSerializer.resolve(ctxt);
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        return defaultSerializer.createContextual(ctxt, property);
    }

}
