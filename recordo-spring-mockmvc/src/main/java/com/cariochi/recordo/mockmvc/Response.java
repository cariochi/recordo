package com.cariochi.recordo.mockmvc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MockMvc response returned by Recordo.
 *
 * @param <T> response body type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private HttpStatus status;
    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();
    private T body;

}
