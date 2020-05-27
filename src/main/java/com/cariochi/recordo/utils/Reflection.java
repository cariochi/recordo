package com.cariochi.recordo.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

public final class Reflection {

    private Reflection() {
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
                .map(field -> Fields.getField(testInstance, field).getValue())
                .map(fieldType::cast);
    }

}
