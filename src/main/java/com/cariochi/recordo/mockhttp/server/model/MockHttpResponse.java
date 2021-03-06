package com.cariochi.recordo.mockhttp.server.model;

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

}
