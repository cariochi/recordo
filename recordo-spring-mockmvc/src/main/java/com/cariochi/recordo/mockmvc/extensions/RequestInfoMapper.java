package com.cariochi.recordo.mockmvc.extensions;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@UtilityClass
public class RequestInfoMapper {

    static RequestInfo mapToRequestInfo(RequestMapping annotation) {

        final HttpMethod httpMethod = Optional.of(annotation.method())
                .filter(ArrayUtils::isNotEmpty)
                .map(v -> v[0])
                .map(RequestMethod::name)
                .map(HttpMethod::valueOf)
                .orElse(null);

        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(httpMethod)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

    static RequestInfo mapToRequestInfo(GetMapping annotation) {
        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(GET)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

    static RequestInfo mapToRequestInfo(PostMapping annotation) {
        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(POST)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

    static RequestInfo mapToRequestInfo(PutMapping annotation) {
        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(PUT)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

    static RequestInfo mapToRequestInfo(PatchMapping annotation) {
        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(PATCH)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

    static RequestInfo mapToRequestInfo(DeleteMapping annotation) {
        final String path = Optional.of(annotation.value()).filter(ArrayUtils::isNotEmpty)
                .or(() -> Optional.of(annotation.path()).filter(ArrayUtils::isNotEmpty))
                .map(v -> v[0])
                .orElse("");

        return RequestInfo.builder()
                .httpMethod(DELETE)
                .path(path)
                .headers(annotation.headers())
                .params(annotation.params())
                .produces(annotation.produces())
                .consumes(annotation.consumes())
                .build();
    }

}
