package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.cariochi.recordo.utils.Format.format;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

public final class Reflection {

    private Reflection() {
    }

    public static Optional<ObjectField> findObjectField(Object target, String fieldName) {
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
                return findObjectField(newTarget, newField);
            } else {
                return Optional.of(new ObjectField(target, field));
            }
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }

    public static Object readField(Object target, String fieldName) {
        final ObjectField objectField = findObjectField(target, fieldName)
                .orElseThrow(() -> new IllegalArgumentException(format("Field '{}' not found", fieldName)));
        return readField(objectField.field(), objectField.object());
    }

    public static void writeField(Object target, String fieldName, Object value) {
        findObjectField(target, fieldName)
                .ifPresent(objectField -> writeField(objectField.field(), objectField.object(), value));
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Method method, Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(method, annotationClass, true, true));
    }

    public static <T> Optional<T> readAnnotatedValue(Object testInstance,
                                                     Class<T> fieldType,
                                                     Class<? extends Annotation> annotationClass) {
        return getFieldsListWithAnnotation(testInstance.getClass(), annotationClass).stream()
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .findFirst()
                .map(Field::getName)
                .map(field -> readField(testInstance, field))
                .map(fieldType::cast);
    }

    private static Object readField(Field field, Object target) {
        try {
            return FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }

    private static void writeField(Field field, Object target, Object value) {
        try {
            FieldUtils.writeField(field, target, value, true);
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }

    public static class ObjectField {

        private final Object object;
        private final Field field;

        public ObjectField(Object object, Field field) {
            this.object = object;
            this.field = field;
        }

        public Object object() {
            return object;
        }

        public Field field() {
            return field;
        }
    }
}
