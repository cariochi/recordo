package com.cariochi.recordo.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparing;

public class SortedSetJsonSerializer extends JsonSerializer<Set> {

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
        for (Object item : getSortedSet(set)) {
            gen.writeObject(item);
        }
        gen.writeEndArray();
    }

    private Set<Object> getSortedSet(Set<?> set) {
        if (set.isEmpty() || SortedSet.class.isAssignableFrom(set.getClass())) {
            return (Set<Object>) set;
        }
        final boolean isComparable = Comparable.class.isAssignableFrom(set.iterator().next().getClass());
        final TreeSet<Object> treeSet = isComparable ? new TreeSet<>() : new TreeSet<>(comparing(Object::hashCode));
        treeSet.addAll(set);
        return treeSet;
    }

}
