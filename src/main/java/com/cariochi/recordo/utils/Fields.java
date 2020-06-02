package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

public final class Fields {

    private Fields() {
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

    public static ObjectField getField(Object target, String fieldName) {
        return findObjectField(target, fieldName)
                .orElseThrow(() -> new IllegalArgumentException(format("Field '{}' not found", fieldName)));
    }

    public static List<ObjectField> getAllFields(Object target) {
        return FieldUtils.getAllFieldsList(target.getClass()).stream()
                .map(field -> new ObjectField(target, field))
                .collect(toList());
    }

    public static List<ObjectField> getFieldsWithAnnotation(Object object, Class<? extends Annotation> annotationCls) {
        return FieldUtils.getFieldsListWithAnnotation(object.getClass(), annotationCls).stream()
                .map(field -> new ObjectField(object, field))
                .collect(toList());
    }

    public static <T> Optional<T> readAnnotatedValue(Object testInstance,
                                                     Class<T> fieldType,
                                                     Class<? extends Annotation> annotationClass) {
        return getFieldsWithAnnotation(testInstance, annotationClass).stream()
                .filter(field -> fieldType.isAssignableFrom(field.getFieldClass()))
                .findFirst()
                .map(ObjectField::getValue)
                .map(fieldType::cast);
    }

    public static class ObjectField {

        private final Object object;
        private final Field field;

        public ObjectField(Object object, Field field) {
            this.object = object;
            this.field = field;
        }

        public Type getFieldType() {
            return field.getGenericType();
        }

        public Class<?> getFieldClass() {
            return field.getType();
        }

        public Class<?> getObjectClass() {
            return object.getClass();
        }

        public String getName() {
            return field.getName();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return field.getDeclaredAnnotation(annotationClass);
        }

        public Object getValue() {
            try {
                return FieldUtils.readField(field, object, true);
            } catch (IllegalAccessException e) {
                throw new RecordoError(e);
            }
        }

        public void setValue(Object value) {
            try {
                FieldUtils.writeField(field, object, value, true);
            } catch (IllegalAccessException e) {
                throw new RecordoError(e);
            }
        }
    }
}
