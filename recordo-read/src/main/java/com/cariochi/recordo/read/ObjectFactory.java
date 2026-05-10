package com.cariochi.recordo.read;

import com.cariochi.objecto.modifiers.ObjectoModifier;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.types.ReflectoType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Loads the fixture and applies all modifications accumulated on this factory.
     *
     * @return fixture instance
     */
    public T create() {
        final T o = (T) objectReader.read(fileName, parameterType);
        return ObjectoModifier.modifyObject(o, fieldValues);
    }

    /**
     * Returns a new factory variant that will set the specified object path before creating the fixture.
     *
     * @param name  object path, for example {@code id}, {@code author.name}, or {@code items[*].sku}
     * @param value replacement value
     * @return new factory with the modification applied
     */
    public ObjectFactory<T> with(String name, Object... value) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.put(name, value);
        return factory;
    }

    /**
     * Loads the fixture with additional object-path modifications.
     *
     * @param fieldValues object paths mapped to replacement values
     * @return fixture instance
     */
    public T createWith(Map<String, Object[]> fieldValues) {
        final ObjectFactory<T> factory = new ObjectFactory<>(this);
        factory.fieldValues.putAll(fieldValues);
        return factory.create();
    }

}
