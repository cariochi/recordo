package com.cariochi.recordo.core.json;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;

public class MaxDepthPropertyFilter extends SimpleBeanPropertyFilter {

    private final static int maxDepth = 3;

    @Override
    public void serializeAsProperty(Object pojo, JsonGenerator gen, SerializationContext ctxt, PropertyWriter writer) throws Exception {
        int depth = calcDepth(writer, gen);
        if (depth <= maxDepth) {
            writer.serializeAsProperty(pojo, gen, ctxt);
        }
        // comment this if you don't want {} placeholders
        else {
            writer.serializeAsOmittedProperty(pojo, gen, ctxt);
        }
    }

    private int calcDepth(PropertyWriter writer, JsonGenerator jgen) {
        TokenStreamContext sc = jgen.streamWriteContext();
        int depth = -1;
        while (sc != null) {
            sc = sc.getParent();
            depth++;
        }
        return depth;
    }

}
