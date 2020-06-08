package com.cariochi.recordo.reflection;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class Fields {

    private final Object target;

    private Fields(Object target) {
        this.target = target;
    }

    public static Fields of(Object target) {
        return new Fields(target);
    }

    public Optional<TargetField> find(String fieldName) {
        if (target == null) {
            return Optional.empty();
        }
        final List<String> split = new ArrayList<>(asList(fieldName.split("\\.")));
        final Field field = FieldUtils.getField(target.getClass(), split.remove(0), true);
        TargetField targetField = new TargetField(target, field);
        for (String f : split) {
            targetField = targetField.get(f);
        }
        return Optional.of(targetField);
    }

    public TargetField get(String fieldName) {
        return find(fieldName)
                .orElseThrow(() -> new IllegalArgumentException(format("Field '{}' not found", fieldName)));
    }

    public List<TargetField> all() {
        return FieldUtils.getAllFieldsList(target.getClass()).stream()
                .map(field -> new TargetField(target, field))
                .collect(toList());
    }

    public List<TargetField> findAll(Class<? extends Annotation> annotationCls) {
        return FieldUtils.getFieldsListWithAnnotation(target.getClass(), annotationCls).stream()
                .map(field -> new TargetField(target, field))
                .collect(toList());
    }

    public List<TargetField> findAll(Class<?> fieldType, Class<? extends Annotation> annotationClass) {
        return findAll(annotationClass).stream()
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .collect(toList());
    }

    public Optional<TargetField> findAny(Class<?> fieldType, Class<? extends Annotation> annotationClass) {
        return findAll(fieldType, annotationClass).stream().findAny();
    }
}
