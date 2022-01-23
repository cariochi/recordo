package com.cariochi.recordo.read;

import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.Reflection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.cariochi.reflecto.Reflecto.reflect;

@Slf4j
@RequiredArgsConstructor
public class ObjectFactory<T> {

    private final ObjectReader objectReader;
    private final String fileName;
    private final Type parameterType;

    private final HashMap<String, Object> fieldValues = new HashMap<>();

    private ObjectFactory(ObjectFactory<T> factory) {
        this(factory.objectReader, factory.fileName, factory.parameterType);
        this.fieldValues.putAll(factory.fieldValues);
    }

    public T create() {
        final Object o = objectReader.read(fileName, parameterType);
        final Reflection reflection = reflect(o);
        fieldValues.forEach((name, value) -> reflection.get(name).setValue(value));
        return (T) o;
    }

    public ObjectFactory<T> with(String name, Object value) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.put(name, value);
        return factory;
    }

    public T createWith(Map<String, Object> fieldValues) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.putAll(fieldValues);
        return factory.create();
    }
}
