package com.cariochi.recordo.core.json;

import java.util.List;
import lombok.Getter;

import static java.util.stream.Collectors.toList;

@Getter
public class JsonFilter {

    private final List<Path> included;
    private final List<Path> excluded;

    public JsonFilter(List<String> included, List<String> excluded) {
        this.included = included.stream().map(Path::new).collect(toList());
        this.excluded = excluded.stream().map(Path::new).collect(toList());
    }

    public boolean hasProperties() {
        return !included.isEmpty() || !excluded.isEmpty();
    }

    public boolean shouldInclude(Path path) {
        return isIncluded(path) && !isExcluded(path);
    }

    private boolean isIncluded(Path path) {
        if (included.isEmpty()) {
            return true;
        }
        return included.stream()
                .anyMatch(pattern -> path.startWith(pattern) || pattern.startWith(path));
    }

    private boolean isExcluded(Path path) {
        if (excluded.isEmpty()) {
            return false;
        }
        return excluded.stream().anyMatch(path::mathes);
    }

}
