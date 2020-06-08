package com.cariochi.recordo.reflection;

import com.cariochi.recordo.RecordoError;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class TargetField {

    private final Object target;
    private final Field field;

    public TargetField(Object target, Field field) {
        this.target = target;
        this.field = field;
    }

    public Fields fields() {
        return Fields.of(getValue());
    }

    public TargetField get(String fieldName) {
        return fields().get(fieldName);
    }

    public String getName() {
        return field.getName();
    }

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return getTarget().getClass();
    }

    public Type getGenericType() {
        return field.getGenericType();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return field.getDeclaredAnnotation(annotationClass);
    }

    public <T> T getValue() {
        try {
            return (T) FieldUtils.readField(field, target, true);
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }

    public <T> void setValue(T value) {
        try {
            FieldUtils.writeField(field, target, value, true);
        } catch (IllegalAccessException e) {
            throw new RecordoError(e);
        }
    }
}
