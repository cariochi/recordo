package com.cariochi.recordo.core;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import static com.cariochi.reflecto.Reflecto.reflect;

@Slf4j
public class EmptyInstanceGenerator {

    public Object createInstance(Type type, int level) {
        if (level == 0) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            final Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
            final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (Collection.class.isAssignableFrom(rawType)) {
                return createCollection(rawType, typeArguments[0], level);
            } else if (Map.class.isAssignableFrom(rawType)) {
                return createMap(rawType, typeArguments[0], typeArguments[1], level);
            }
            return createSimple(rawType, level);
        } else if (type instanceof Class) {
            final Class<?> clss = (Class<?>) type;
            if (clss.isEnum()) {
                return createEnum(clss);
            } else if (clss.isArray()) {
                return createArray(clss, level);
            } else {
                return createSimple(clss, level);
            }
        } else {
            return null;
        }
    }

    private Collection<Object> createCollection(Class<?> collectionType, Type componentType, int level) {
        if (Set.class.isAssignableFrom(collectionType)) {
            final Set<Object> set = Set.class.equals(collectionType)
                    ? new HashSet<>()
                    : (Set<Object>) createInstance(collectionType, level);
            set.add(createInstance(componentType, level));
            return set;
        } else if (List.class.isAssignableFrom(collectionType)) {
            final List<Object> list = List.class.equals(collectionType)
                    ? new ArrayList<>()
                    : (List<Object>) createInstance(collectionType, level);
            list.add(createInstance(componentType, level));
            return list;
        } else {
            return null;
        }
    }

    private Map<Object, Object> createMap(Class<?> mapType, Type keyType, Type valueType, int level) {
        final Map<Object, Object> map = Map.class.equals(mapType)
                ? new HashMap<>()
                : (Map<Object, Object>) createInstance(mapType, level);
        map.put(createInstance(keyType, level), createInstance(valueType, level));
        return map;
    }

    private Object createSimple(Class<?> type, int level) {

        Object instance = Stream.of(type.getConstructors())
                .filter(c -> c.getParameterCount() == 0)
                .findAny()
                .map(this::newInstanceFromConstructor)
                .orElse(null);

        if (instance == null) {
            instance = Stream.of(type.getConstructors())
                    .filter(c -> c.getParameterCount() == 1)
                    .filter(c -> String.class.isAssignableFrom(c.getParameterTypes()[0]))
                    .findAny()
                    .map(constructor -> newInstanceFromConstructor(constructor, "0"))
                    .orElse(null);
        }

        if (instance == null) {
            instance = Stream.of(type.getMethods())
                    .filter(method -> Modifier.isStatic(method.getModifiers()))
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> type.isAssignableFrom(method.getReturnType()))
                    .findAny()
                    .map(this::newInstanceFromStaticMethod)
                    .orElse(null);
        }

        if (instance != null) {
            reflect(instance).fields().all().stream()
                    .filter(field -> !field.isStatic())
                    .filter(field -> !field.isPrimitive())
                    .filter(field -> !field.isTransient())
                    .filter(field -> field.getValue() == null)
                    .forEach(field -> field.setValue(createInstance(field.getGenericType(), level - 1)));
        }

        return instance;
    }

    private Object createArray(Class<?> type, int level) {
        final Class<?> componentType = type.getComponentType();
        final Object array = Array.newInstance(componentType, 1);
        if (!componentType.isPrimitive()) {
            Array.set(array, 0, createInstance(componentType, level));
        }
        return array;
    }

    private Enum<?> createEnum(Class<?> type) {
        try {
            return ((Enum<?>[]) type.getMethod("values").invoke(null))[0];
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn(e.getCause().toString());
            return null;
        }
    }

    private Object newInstanceFromStaticMethod(Method method) {
        try {
            method.setAccessible(true);
            return method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn(e.getCause().toString());
            return null;
        }
    }

    private Object newInstanceFromConstructor(Constructor<?> constructor, Object... parameters) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.warn(e.getCause().toString());
            return null;
        }
    }

}
