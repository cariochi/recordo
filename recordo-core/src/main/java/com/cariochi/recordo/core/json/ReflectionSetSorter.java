package com.cariochi.recordo.core.json;

import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.fields.JavaField;

import java.util.*;
import java.util.function.Predicate;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.emptyList;
import static java.util.Comparator.nullsLast;
import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.groupingBy;

public class ReflectionSetSorter {

    public static final List<Predicate<JavaField>> FIELD_NAMES_PREDICATES = List.of(
            field -> "id".equalsIgnoreCase(field.getName()),
            field -> "uid".equalsIgnoreCase(field.getName()),
            field -> "uuid".equalsIgnoreCase(field.getName()),
            field -> "name".equalsIgnoreCase(field.getName()),
            field -> field.getName().endsWith("Id"),
            field -> field.getName().endsWith("Uid"),
            field -> field.getName().endsWith("Uuid"),
            field -> field.getName().endsWith("Name"),
            field -> String.class.isAssignableFrom(field.getType())
    );

    public <T> Set<T> sort(Set<T> set) {
        if (set.isEmpty() || SortedSet.class.isAssignableFrom(set.getClass())) {
            return set;
        }
        final Object first = set.iterator().next();
        final boolean isComparable = Comparable.class.isAssignableFrom(first.getClass());
        if (isComparable) {
            return new TreeSet<>(set);
        } else {
            final Comparator<Object> comparator = comparator(first, set);
            if (comparator == null) {
                return set;
            } else {
                final TreeSet<T> treeSet = new TreeSet<>(comparator);
                treeSet.addAll(set);
                return treeSet;
            }
        }
    }

    private <T, U extends Comparable<? super U>> Comparator<T> comparator(T object, Set<?> set) {
        final List<JavaField> fields = reflect(object).fields().all();
        return findMostUniqueField(set, fields)
                .map(field -> nullsLast(Comparator.<T, U>comparing(o -> reflect(o).field(field).getValue())))
                .orElse(null);
    }

    private <T> Optional<String> findMostUniqueField(Set<T> set, List<JavaField> fields) {
        final List<JavaField> mostUniqueFields = fields.stream()
                .filter(field -> field.isPrimitive() || Comparable.class.isAssignableFrom(field.getType()))
                .collect(groupingBy(field -> getUniqueValuesAmount(field.getName(), set))).entrySet().stream()
                .max(comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(emptyList());
        for (JavaField field : mostUniqueFields) {
            for (Predicate<JavaField> predicate : FIELD_NAMES_PREDICATES) {
                if (predicate.test(field)) {
                    return Optional.of(field).map(JavaField::getName);
                }
            }
        }
        return mostUniqueFields.stream().map(JavaField::getName).min(CASE_INSENSITIVE_ORDER);
    }

    private <T> long getUniqueValuesAmount(String field, Set<T> set) {
        return set.stream()
                .map(Reflecto::reflect)
                .map(reflection -> reflection.field(field).getValue())
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

}
