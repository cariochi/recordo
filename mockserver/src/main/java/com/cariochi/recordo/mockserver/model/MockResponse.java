package com.cariochi.recordo.mockserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_EMPTY)
public class MockResponse {

    private String protocol;

    private Integer statusCode;

    private String statusText;

    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();

    private Object body;

    public String contentType() {
        return headers.entrySet().stream()
                .filter(e -> "content-type".equalsIgnoreCase(e.getKey()))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse("application/json");
    }

    @JsonIgnore
    public boolean isJson() {
        return contentType().startsWith("application/json");
    }
}
