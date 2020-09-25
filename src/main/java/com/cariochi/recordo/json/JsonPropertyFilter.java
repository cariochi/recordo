package com.cariochi.recordo.json;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class JsonPropertyFilter {

    private final List<String> included;
    private final List<String> excluded;

    public boolean hasProperties() {
        return !included.isEmpty() || !excluded.isEmpty();
    }

    public boolean shouldInclude(String field) {
        return isIncluded(field) && !isExcluded(field);
    }

    private boolean isIncluded(String field) {
        if (included.isEmpty()) {
            return true;
        }
        return included.stream()
                .anyMatch(f -> f.equals(field) || f.startsWith(field + ".") || field.startsWith(f + "."));
    }

    private boolean isExcluded(String field) {
        if (excluded.isEmpty()) {
            return false;
        }
        return excluded.stream().anyMatch(f -> f.equals(field));
    }

}
