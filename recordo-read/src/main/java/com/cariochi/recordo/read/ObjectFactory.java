package com.cariochi.recordo.read;

import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.Reflection;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

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
        final Object o = objectReader.read(fileName, parameterType);
        final Reflection reflection = reflect(o);

        final Map<String, Object> values = isCollection(o) || isArray(o)
                ? fieldValues.entrySet().stream().map(e -> Map.entry("[*]." + e.getKey(), e.getValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                : fieldValues;

        values.entrySet().stream()
                .map(e -> splitName(e.getKey(), e.getValue(), reflection))
                .flatMap(map -> map.entrySet().stream())
                .forEach(e -> reflection.get(e.getKey()).setValue(e.getValue()));

        return (T) o;
    }

    private Map<String, Object> splitName(String name, Object value, Reflection reflection) {
        if (name.contains("[*]")) {

            final String prefix = substringBefore(name, "[*]");
            final String suffix = substringAfter(name, "[*]");

            final Reflection javaField = isEmpty(prefix) ? reflection : reflection.get(prefix);

            int size = getSize(javaField.getValue());

            final Map<String, Object> resultMap = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                final String key = prefix + "[" + i + "]" + suffix;
                final Map<String, Object> split = splitName(key, value, reflection);
                resultMap.putAll(split);
            }
            return resultMap;

        } else {
            return Map.of(name, value);
        }
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

    private int getSize(Object o) {
        if (isArray(o)) {
            return Array.getLength(o);
        } else if (isCollection(o)) {
            return ((Collection<?>) o).size();
        } else {
            return -1;
        }
    }

    private static boolean isArray(Object o) {
        return o.getClass().isArray();
    }

    private static boolean isCollection(Object o) {
        return Collection.class.isAssignableFrom(o.getClass());
    }

}
