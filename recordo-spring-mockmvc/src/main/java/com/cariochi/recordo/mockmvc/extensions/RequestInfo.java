package com.cariochi.recordo.mockmvc.extensions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Value
@Builder
class RequestInfo {

    HttpMethod httpMethod;
    String path;
    String[] headers;
    String[] params;
    String[] produces;
    String[] consumes;

    public MultiValueMap<String, String> getParams() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (params == null) {
            return map;
        }
        for (String p : params) {
            final String[] split = p.split("=");
            map.put(split[0].trim(), List.of(split[1].trim()));
        }
        return map;
    }

    public Map<String, String> getHeaders() {
        final Map<String, String> map = new HashMap<>();
        getContentTypeHeader().ifPresent(contentType -> map.put(HttpHeaders.CONTENT_TYPE, contentType));
        getAcceptHeader().ifPresent(contentType -> map.put(HttpHeaders.ACCEPT, contentType));
        if (headers == null) {
            return map;
        }
        Arrays.stream(headers).map(param -> param.split("=")).forEach(pair -> map.put(pair[0].trim(), pair[1].trim()));
        return map;
    }

    public RequestInfo applyBaseInfo(RequestInfo baseRequestInfo) {
        if (baseRequestInfo == null) {
            return this;
        }
        return RequestInfo.builder()
                .httpMethod(httpMethod)
                .path(Optional.ofNullable(baseRequestInfo.path).map(p -> p + path).orElse(path))
                .headers(unite(headers, baseRequestInfo.headers))
                .params(unite(params, baseRequestInfo.params))
                .produces(unite(produces, baseRequestInfo.produces))
                .consumes(unite(consumes, baseRequestInfo.consumes))
                .build();
    }

    private static String[] unite(String[] first, String[] second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        } else {
            return Stream.of(first, second).flatMap(Stream::of).distinct().toArray(String[]::new);
        }
    }

    public Optional<String> getContentTypeHeader() {
        return Optional.ofNullable(consumes)
                .filter(ArrayUtils::isNotEmpty)
                .map(a -> a[0]);
    }

    public Optional<String> getAcceptHeader() {
        return Optional.ofNullable(produces)
                .filter(ArrayUtils::isNotEmpty)
                .map(a -> a[0]);
    }

}
