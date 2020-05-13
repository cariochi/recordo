package com.cariochi.recordo.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;

@UtilityClass
public class ReflectionUtils {

    @SneakyThrows
    public static Optional<Pair<Field, Object>> getFieldAndTargetObject(Object target, String fieldName) {
        if (target == null) {
            return Optional.empty();
        }

        final String currentField = substringBefore(fieldName, ".");

        final Field field = FieldUtils.getField(target.getClass(), currentField, true);
        if (field == null) {
            return Optional.empty();
        }

        final String newField = substringAfter(fieldName, ".");
        if (isNotBlank(newField)) {
            final Object newTarget = FieldUtils.readField(field, target);
            return getFieldAndTargetObject(newTarget, newField);
        } else {
            return Optional.of(Pair.of(field, target));
        }

    }

    @SneakyThrows
    public static Object readField(Object target, String fieldName) {
        final Pair<Field, Object> fieldAndTargetObject = getFieldAndTargetObject(target, fieldName)
                .orElseThrow(() -> new IllegalArgumentException(format("Field %s not found", fieldName)));
        return readField(fieldAndTargetObject.getLeft(), fieldAndTargetObject.getRight());
    }

    @SneakyThrows
    public static void writeField(Object target, String fieldName, Object value) {
        getFieldAndTargetObject(target, fieldName)
                .ifPresent(p -> writeField(p.getLeft(), p.getRight(), value));
    }

    @SneakyThrows
    private static Object readField(Field field, Object target) {
        return FieldUtils.readField(field, target, true);
    }

    @SneakyThrows
    public static void writeField(Field field, Object target, Object value) {
        FieldUtils.writeField(field, target, value, true);
    }
}
