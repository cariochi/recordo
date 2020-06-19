package com.cariochi.recordo.httpmock.model;

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
    private Map<String, String> headers = new LinkedHashMap<>();
    private Object body;

}
