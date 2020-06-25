package com.cariochi.recordo.utils;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.apache.commons.lang3.reflect.MethodUtils.getAnnotation;

@UtilityClass
public class Reflection {

    public static <A extends Annotation> Optional<A> findAnnotation(Method method, Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(method, annotationClass, true, true));
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Class<?> type, Class<A> annotationClass) {
        return Optional.ofNullable(type.getAnnotation(annotationClass));
    }

    public static void checkClassLoaded(String className) throws ClassNotFoundException {
        Class.forName(className, false, Reflection.class.getClassLoader());
    }
}
