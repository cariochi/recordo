package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Set;

public class SortedSetJsonSerializer extends StdSerializer<Set> {

    private final ReflectionSetSorter setSorter = new ReflectionSetSorter();

    protected SortedSetJsonSerializer() {
        super(Set.class);
    }

    @Override
    public void serialize(Set set, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (set == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartArray();
        for (Object item : setSorter.sort(set)) {
            gen.writeObject(item);
        }
        gen.writeEndArray();
    }

}
