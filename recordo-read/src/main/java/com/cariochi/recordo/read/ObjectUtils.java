package com.cariochi.recordo.read;

import com.cariochi.reflecto.Reflection;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@UtilityClass
public class ObjectUtils {

    public static <T> T modifyObject(T object, Map<String, Object> fieldValues) {
        final Reflection reflection = reflect(object);

        final Map<String, Object> values = isCollection(object) || isArray(object)
                ? fieldValues.entrySet().stream().map(e -> Map.entry("[*]." + e.getKey(), e.getValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                : fieldValues;

        values.entrySet().stream()
                .map(e -> splitName(e.getKey(), e.getValue(), reflection))
                .flatMap(map -> map.entrySet().stream())
                .forEach(e -> reflection.get(e.getKey()).setValue(e.getValue()));

        return object;
    }

    private static Map<String, Object> splitName(String name, Object value, Reflection reflection) {
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

    private static int getSize(Object o) {
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
