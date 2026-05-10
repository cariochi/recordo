package com.cariochi.recordo.core.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Set;

public class SortedSetJsonSerializer extends StdSerializer<Set> {

    private final ReflectionSetSorter setSorter = new ReflectionSetSorter();

    protected SortedSetJsonSerializer() {
        super(Set.class);
    }

    @Override
    public void serialize(Set set, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (set == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartArray();
        for (Object item : setSorter.sort(set)) {
            ctxt.writeValue(gen, item);
        }
        gen.writeEndArray();
    }

}
