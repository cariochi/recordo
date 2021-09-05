package com.cariochi.recordo.mockmvc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.*;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.NONE;
import static org.apache.commons.lang3.ArrayUtils.addFirst;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Request<RESP> {

    private final RecordoMockMvc client;
    private final HttpMethod method;
    private final String path;
    private final Type responseType;

    @Setter(NONE)
    private Map<String, String> headers = new LinkedHashMap<>();

    @Setter(NONE)
    private Object[] uriVars = {};

    @Setter(NONE)
    private MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

    private Object body;

    private HttpStatus expectedStatus;

    public Response<RESP> execute() {
        return client.execute(this);
    }

    public Request<RESP> headers(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    public Request<RESP> header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Request<RESP> uriVars(Object pathVar, Object... pathVars) {
        uriVars = addFirst(pathVars, pathVar);
        return this;
    }

    public Request<RESP> params(MultiValueMap<String, String> params) {
        params.forEach(this::addToParams);
        return this;
    }

    public Request<RESP> param(String name, String... values) {
        addToParams(name, asList(values));
        return this;
    }

    private void addToParams(String name, List<String> values) {
        values.forEach(value -> params.add(name, value));
    }

}
