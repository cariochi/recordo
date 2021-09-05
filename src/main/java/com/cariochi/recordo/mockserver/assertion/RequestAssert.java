package com.cariochi.recordo.mockserver.assertion;

import com.cariochi.recordo.mockserver.model.MockHttpRequest;
import lombok.experimental.UtilityClass;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.net.URI;
import java.util.Map;

import static com.cariochi.recordo.utils.Format.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@UtilityClass
public class RequestAssert {

    private final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private final String APPLICATION_JSON = "application/json";
    private final String CONTENT_TYPE = "content-type";

    public void assertEquals(MockHttpRequest expected, MockHttpRequest actual) {
        assertStrings("method", expected.getMethod(), actual.getMethod());
        assertUrls(expected.getUrl(), actual.getUrl());
        assertMaps("headers", expected.getHeaders(), actual.getHeaders());
        assertBodies(expected, actual);
    }

    private void assertUrls(String expected, String actual) {
        final URI expectedUri = URI.create(expected);
        final URI actualUri = URI.create(actual);
        assertStrings("host", expectedUri.getHost(), actualUri.getHost());
        assertStrings("path", expectedUri.getPath(), actualUri.getPath());
        assertMaps("query", parseQuery(expectedUri.getQuery()), parseQuery(actualUri.getQuery()));
    }

    private void assertBodies(MockHttpRequest expected, MockHttpRequest actual) {
        if (expected == null && actual == null) {
            return;
        }
        final String expectedContent = (String) expected.getBody();
        final String actualContent = (String) actual.getBody();
        if (expectedContent == null && actualContent == null) {
            return;
        }
        final String contentType = contentType(expected.getHeaders());
        if (contentType == null) {
            assertJson("body", expectedContent, actualContent);
        } else if (contentType.startsWith(APPLICATION_JSON)) {
            assertJson("body", expectedContent, actualContent);
        } else if (contentType.startsWith(APPLICATION_FORM_URLENCODED)) {
            assertMaps("query", parseQuery(expectedContent), parseQuery(actualContent));
        } else {
            assertStrings("body", expectedContent, actualContent);
        }
    }

    private void assertMaps(String name, Map<String, String> expected, Map<String, String> actual) {
        if (expected == null && actual == null) {
            return;
        }
        assertJson(name, toJson(expected), toJson(actual));
    }

    private String toJson(Map<String, String> expected) {
        return "{"
               + expected.entrySet().stream()
                       .map(e -> format("\"{}\":\"{}\"", e.getKey(), e.getValue()))
                       .collect(joining(","))
               + "}";
    }

    private void assertStrings(String name, String expected, String actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (!expected.equalsIgnoreCase(actual)) {
            throw new AssertionError(format("Unexpected {}:\nexpected: {},\nactual: {}", name, expected, actual));
        }
    }

    private void assertJson(String name, String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
        } catch (JSONException e) {
            assertStrings(name, expected, actual);
        } catch (AssertionError e) {
            throw new AssertionError(
                    format("Unexpected {}:\n{}", name, e.getMessage()),
                    e.getCause()
            );
        }
    }

    private Map<String, String> parseQuery(String query) {
        return query == null
                ? emptyMap()
                : stream(query.split("&"))
                .collect(toMap(
                        s -> substringBefore(s, "="),
                        s -> substringAfter(s, "=")
                ));
    }

    private String contentType(Map<String, String> headers) {
        return headers.entrySet().stream()
                .filter(e -> CONTENT_TYPE.equalsIgnoreCase(e.getKey()))
                .findAny()
                .map(Map.Entry::getValue)
                .map(h -> substringBefore(h, ";"))
                .orElse(null);
    }
}
