package com.cariochi.recordo.core.json;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.cariochi.recordo.core.json.ReflectionSetSorterTest.Enum.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReflectionSetSorterTest {

    private final ReflectionSetSorter sorter = new ReflectionSetSorter();

    @Test
    void should_sort_strings() {
        final Set<String> sorted = sorter.sort(Set.of("C", "B", "A"));
        assertThat(sorted).containsExactly("A", "B", "C");
    }

    @Test
    void should_sort_enum() {
        final Set<Enum> sorted = sorter.sort(Set.of(Third, Second, First));
        assertThat(sorted).containsExactly(First, Second, Third);
    }

    @Test
    void should_sort_by_id() {
        final Set<WithId> sorted = sorter.sort(Set.of(new WithId(3L, "A"), new WithId(1L, "B"), new WithId(2L, "C")));
        assertThat(sorted)
                .extracting(WithId::getId)
                .containsExactly(1L, 2L, 3L);
    }

    @Value
    @RequiredArgsConstructor
    private static class WithId {

        Long id;
        String name;

    }

    enum Enum {
        First, Second, Third
    }

}
