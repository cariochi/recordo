package com.cariochi.recordo.restmocks.model;

import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class RequestMock {

    private String method;
    private String url;
    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();
    private Object body;

}
