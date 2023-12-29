package com.cariochi.recordo.read;

import com.cariochi.objecto.utils.ObjectUtils;
import com.cariochi.recordo.core.utils.ObjectReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ObjectFactory<T> {

    private final ObjectReader objectReader;
    private final String fileName;
    private final Type parameterType;

    private final Map<String, Object> fieldValues = new HashMap<>();

    private ObjectFactory(ObjectFactory<T> factory) {
        this(factory.objectReader, factory.fileName, factory.parameterType);
        this.fieldValues.putAll(factory.fieldValues);
    }

    public T create() {
        final T o = (T) objectReader.read(fileName, parameterType);
        return ObjectUtils.modifyObject(o, fieldValues);
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
