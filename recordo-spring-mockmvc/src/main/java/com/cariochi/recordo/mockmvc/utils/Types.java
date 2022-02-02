package com.cariochi.recordo.mockmvc.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.springframework.core.ParameterizedTypeReference.forType;

@UtilityClass
public class Types {

    public static <P> TypeBuilder<P> get(Class<P> aClass) {
        return new TypeBuilder<>(aClass);
    }

    public static <T> ParameterizedTypeReference<T> typeOf(Class<T> aClass) {
        return forType(aClass);
    }

    public static <T> ParameterizedTypeReference<Page<T>> pageOf(Class<T> aClass) {
        return forType(parameterize(Page.class, aClass));
    }

    public static <T> ParameterizedTypeReference<List<T>> listOf(Class<T> aClass) {
        return forType(parameterize(List.class, aClass));
    }

    public static <T> ParameterizedTypeReference<Set<T>> setOf(Class<T> aClass) {
        return forType(parameterize(Set.class, aClass));
    }

    public static <K, V> ParameterizedTypeReference<Map<K, V>> mapOf(Class<K> keyClass, Class<V> valueClass) {
        return forType(parameterize(Map.class, keyClass, valueClass));
    }

    @Data
    @AllArgsConstructor
    public static class TypeBuilder<P> {

        private Class<P> pClass;

        public ParameterizedType of(Type... types) {
            return parameterize(pClass, types);
        }

    }

}
