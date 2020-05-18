package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

public final class ReflectionUtils {

    private ReflectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Optional<Pair<Field, Object>> getFieldAndTargetObject(Object target, String fieldName) {
        if (target == null) {
            return Optional.empty();
        }

        try {
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
        } catch (IllegalAccessException e) {
            throw new RecordoException(e);
        }
    }

    public static Object readField(Object target, String fieldName) {
        final Pair<Field, Object> fieldAndTargetObject = getFieldAndTargetObject(target, fieldName)
                .orElseThrow(() -> new IllegalArgumentException(format("Field %s not found", fieldName)));
        return readField(fieldAndTargetObject.getLeft(), fieldAndTargetObject.getRight());
    }

    public static void writeField(Object target, String fieldName, Object value) {
        getFieldAndTargetObject(target, fieldName)
                .ifPresent(p -> writeField(p.getLeft(), p.getRight(), value));
    }

    public static <A extends Annotation> Stream<A> findAnnotation(Method method, Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(method, annotationClass, true, true))
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }

    private static Object readField(Field field, Object target) {
        try {
            return FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            throw new RecordoException(e);
        }
    }

    private static void writeField(Field field, Object target, Object value) {
        try {
            FieldUtils.writeField(field, target, value, true);
        } catch (IllegalAccessException e) {
            throw new RecordoException(e);
        }
    }
}
