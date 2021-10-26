package com.cariochi.recordo.read;

import com.cariochi.recordo.core.utils.ObjectReader;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.text.StringSubstitutor.replace;

@RequiredArgsConstructor
public class ObjectTemplate<T> {

    private final ObjectReader objectReader;
    private final String fileName;
    private final Type parameterType;

    private final HashMap<String, Object> variables = new HashMap<>();

    public ObjectTemplate(ObjectTemplate template) {
        this(template.objectReader, template.fileName, template.parameterType);
        this.variables.putAll(template.variables);
    }

    public ObjectTemplate<T> with(String name, Object value) {
        final ObjectTemplate<T> factory = new ObjectTemplate<>(this);
        factory.variables.put(name, value);
        return factory;
    }

    public T create() {
        return (T) objectReader.read(fileName, parameterType, this::applyVariables);
    }

    public T createWith(Map<String, Object> variables) {
        final ObjectTemplate<T> factory = new ObjectTemplate<>(this);
        factory.variables.putAll(variables);
        return factory.create();
    }

    private String applyVariables(String string) {
        return replace(string, variables);
    }

}
