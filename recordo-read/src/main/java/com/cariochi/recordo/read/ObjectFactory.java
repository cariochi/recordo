package com.cariochi.recordo.read;

import com.cariochi.objecto.modifiers.ObjectoModifier;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.types.ReflectoType;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ObjectFactory<T> {

    private final ObjectReader objectReader;
    private final String fileName;
    private final ReflectoType parameterType;

    private final Map<String, Object[]> fieldValues = new HashMap<>();

    private ObjectFactory(ObjectFactory<T> factory) {
        this(factory.objectReader, factory.fileName, factory.parameterType);
        this.fieldValues.putAll(factory.fieldValues);
    }

    public T create() {
        final T o = (T) objectReader.read(fileName, parameterType);
        return ObjectoModifier.modifyObject(o, fieldValues);
    }

    public ObjectFactory<T> with(String name, Object... value) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.put(name, value);
        return factory;
    }

    public T createWith(Map<String, Object[]> fieldValues) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.putAll(fieldValues);
        return factory.create();
    }

}
