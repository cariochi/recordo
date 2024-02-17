package com.cariochi.recordo.core.json;

import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.fields.TargetField;
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
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

public class ReflectionSetSorter implements Serializable {

    public static final List<Predicate<TargetField>> FIELD_NAMES_PREDICATES = List.of(
            field -> "id".equalsIgnoreCase(field.name()),
            field -> "uid".equalsIgnoreCase(field.name()),
            field -> "uuid".equalsIgnoreCase(field.name()),
            field -> "name".equalsIgnoreCase(field.name()),
            field -> field.name().endsWith("Id"),
            field -> field.name().endsWith("Uid"),
            field -> field.name().endsWith("Uuid"),
            field -> field.name().endsWith("Name"),
            field -> field.type().is(String.class)
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
        final List<TargetField> fields = reflect(object).fields().list();
        return findUniqueField(set, fields)
                .map(fieldName -> comparing(
                        o -> (U) reflect(o).fields().find(fieldName).map(TargetField::getValue).orElse(null),
                        nullsLast(naturalOrder())
                ));
    }

    private <T> Optional<String> findUniqueField(Set<T> set, List<TargetField> fields) {
        final List<TargetField> uniqueFields = fields.stream()
                .filter(field -> field.type().isPrimitive() || field.type().is(Comparable.class))
                .filter(field -> hasUniqueValues(field.name(), set))
                .collect(toList());
        for (TargetField field : uniqueFields) {
            for (Predicate<TargetField> predicate : FIELD_NAMES_PREDICATES) {
                if (predicate.test(field)) {
                    return Optional.of(field).map(TargetField::name);
                }
            }
        }
        return uniqueFields.stream().map(TargetField::name).min(CASE_INSENSITIVE_ORDER);
    }

    private <T> boolean hasUniqueValues(String field, Set<T> set) {
        return set.stream()
                       .map(Reflecto::reflect)
                       .flatMap(reflection -> reflection.fields().find(field).stream())
                       .map(TargetField::getValue)
                       .distinct()
                       .count() == set.size();
    }

}
