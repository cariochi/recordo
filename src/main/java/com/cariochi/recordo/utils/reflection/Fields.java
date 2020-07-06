package com.cariochi.recordo.utils.reflection;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(staticName = "of")
public class Fields {

    private final Object target;

    public TargetField get(String fieldName) {
        final Field field = FieldUtils.getField(target.getClass(), fieldName, true);
        return new TargetField(target, field);
    }

    public List<TargetField> all() {
        return FieldUtils.getAllFieldsList(target.getClass()).stream()
                .map(field -> new TargetField(target, field))
                .collect(toList());
    }

    public List<TargetField> withType(Class<?> fieldType) {
        return all().stream()
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .collect(toList());
    }

    public List<TargetField> withAnnotation(Class<? extends Annotation> annotationCls) {
        return FieldUtils.getFieldsListWithAnnotation(target.getClass(), annotationCls).stream()
                .map(field -> new TargetField(target, field))
                .collect(toList());
    }

    public List<TargetField> withTypeAndAnnotation(Class<?> fieldType,
                                                   Class<? extends Annotation> annotationClass) {
        return withAnnotation(annotationClass).stream()
                .filter(field -> fieldType.isAssignableFrom(field.getType()))
                .collect(toList());
    }

}
