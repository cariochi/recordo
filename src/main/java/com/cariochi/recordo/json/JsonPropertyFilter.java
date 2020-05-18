package com.cariochi.recordo.json;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class JsonPropertyFilter {

    private List<String> included = new ArrayList<>();
    private List<String> excluded = new ArrayList<>();

    public JsonPropertyFilter() {
    }

    public JsonPropertyFilter(List<String> included, List<String> excluded) {
        this.included = included;
        this.excluded = excluded;
    }

    public JsonPropertyFilter next(String root) {
        return new JsonPropertyFilter(nextPaths(included, root), nextPaths(excluded, root));
    }

    public boolean hasProperties() {
        return !included.isEmpty() || !excluded.isEmpty();
    }

    public boolean shouldExclude(String field) {
        return !isIncluded(field) || isExcluded(field);
    }

    private boolean isIncluded(String field) {
        return included.isEmpty() || containsRoot(included, field);
    }

    private boolean isExcluded(String field) {
        return containsRoot(excluded, field) && nextPaths(excluded, field).isEmpty();
    }

    private static List<String> nextPaths(List<String> paths, String root) {
        return paths.stream()
                .filter(path -> rootOf(path).equalsIgnoreCase(root))
                .map(JsonPropertyFilter::nextPath)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private static boolean containsRoot(List<String> paths, String root) {
        return paths.stream().anyMatch(path -> rootOf(path).equalsIgnoreCase(root));
    }

    private static String rootOf(String path) {
        return substringBefore(path, ".");
    }

    private static Optional<String> nextPath(String path) {
        return Optional.ofNullable(substringAfter(path, ".")).filter(StringUtils::isNotBlank);
    }

}
