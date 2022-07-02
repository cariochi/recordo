package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Set;

public class SortedSetJsonSerializer extends JsonSerializer<Set> {

    private final ReflectionSetSorter setSorter = new ReflectionSetSorter();

    public static SimpleModule getJsonModule() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Set.class, new SortedSetJsonSerializer());
        return module;
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
