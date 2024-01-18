package com.cariochi.recordo.core.json;

import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.fields.JavaField;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

public class ReflectionSetSorter implements Serializable {

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
            return comparator(first, set)
                    .map(comparator -> {
                        Set<T> treeSet = new TreeSet<>(comparator);
                        treeSet.addAll(set);
                        return treeSet;
                    })
                    .orElse(set);
        }
    }

    private <T, U extends Comparable<? super U>> Optional<Comparator<T>> comparator(T object, Set<?> set) {
        final List<JavaField> fields = reflect(object).fields().asList();
        return findUniqueField(set, fields)
                .map(field -> Comparator.<T, U>comparing(o -> reflect(o).field(field).getValue(), nullsLast(naturalOrder())));
    }

    private <T> Optional<String> findUniqueField(Set<T> set, List<JavaField> fields) {
        final List<JavaField> uniqueFields = fields.stream()
                .filter(field -> field.isPrimitive() || Comparable.class.isAssignableFrom(field.getType()))
                .filter(field -> hasUniqueValues(field.getName(), set))
                .collect(toList());
        for (JavaField field : uniqueFields) {
            for (Predicate<JavaField> predicate : FIELD_NAMES_PREDICATES) {
                if (predicate.test(field)) {
                    return Optional.of(field).map(JavaField::getName);
                }
            }
        }
        return uniqueFields.stream().map(JavaField::getName).min(CASE_INSENSITIVE_ORDER);
    }

    private <T> boolean hasUniqueValues(String field, Set<T> set) {
        return set.stream()
                .map(Reflecto::reflect)
                .map(reflection -> reflection.field(field).getValue())
                .distinct()
                .count() == set.size();
    }

}
