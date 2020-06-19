package com.cariochi.recordo.mockmvc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private Integer statusCode;
    private Map<String, String> headers = new LinkedHashMap<>();
    private T body;

}
