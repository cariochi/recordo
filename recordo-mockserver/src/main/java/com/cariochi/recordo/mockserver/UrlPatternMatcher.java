package com.cariochi.recordo.mockserver;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class UrlPatternMatcher {

    private final Pattern pattern;

    public UrlPatternMatcher(String pattern) {
        this.pattern = toRegexp(pattern);
    }

    private Pattern toRegexp(String pattern) {
        return Optional.of(pattern)
                .map(p -> "\\Q" + p + "\\E")
                .map(p -> replace(p, "?", "\\E.\\Q"))
                .map(p -> replace(p, "**", "_ANY_DIRECTORIES_"))
                .map(p -> replace(p, "*", "_ANY_CHARACTERS_"))
                .map(p -> replace(p, "_ANY_DIRECTORIES_", "\\E.*\\Q"))
                .map(p -> replace(p, "_ANY_CHARACTERS_", "\\E[^/]*\\Q"))
                .map(p -> Pattern.compile(p, CASE_INSENSITIVE))
                .get();
    }

    public boolean match(String url) {
        return pattern.matcher(substringBefore(url, "?")).matches();
    }

}
