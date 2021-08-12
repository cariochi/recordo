package com.cariochi.recordo.mockhttp.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class MockHttpResponse {

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
