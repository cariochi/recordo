package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

public class MaxDepthPropertyFilter extends SimpleBeanPropertyFilter {

    private final static int maxDepth = 3;

    @Override
    public void serializeAsField(Object pojo, JsonGenerator gen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        int depth = calcDepth(writer, gen);
        if (depth <= maxDepth) {
            writer.serializeAsField(pojo, gen, provider);
        }
        // comment this if you don't want {} placeholders
        else {
            writer.serializeAsOmittedField(pojo, gen, provider);
        }
    }

    private int calcDepth(PropertyWriter writer, JsonGenerator jgen) {
        JsonStreamContext sc = jgen.getOutputContext();
        int depth = -1;
        while (sc != null) {
            sc = sc.getParent();
            depth++;
        }
        return depth;
    }

}
