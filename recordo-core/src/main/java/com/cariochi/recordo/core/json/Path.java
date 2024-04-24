package com.cariochi.recordo.core.json;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Path {

    private final List<String> items;

    public Path(String path) {
        items = List.of(split(replace(path, "[", ".["), "."));
    }

    public boolean startWith(Path path) {
        for (int i = 0; i < Math.min(items.size(), path.items.size()); i++) {
            final String item1 = items.get(i);
            final String item2 = path.items.get(i);
            if (!match(item1, item2)) {
                return false;
            }
        }
        return true;
    }

    public boolean mathes(Path path) {
        return startWith(path) && items.size() == path.items.size();
    }

    private boolean match(String item1, String item2) {
        if (item1.equals(item2)) {
            return true;
        }
        if (item1.equals("[*]") && item2.startsWith("[")) {
            return true;
        }
        if (item2.equals("[*]") && item1.startsWith("[")) {
            return true;
        }
        return false;
    }
}
