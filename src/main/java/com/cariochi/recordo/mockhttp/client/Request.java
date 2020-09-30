package com.cariochi.recordo.mockhttp.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.NONE;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Request<RESP> {

    private final MockHttpClient client;
    private final HttpMethod method;
    private final String path;
    private final Type responseType;

    @Setter(NONE)
    private Map<String, String> headers = new LinkedHashMap<>();

    @Setter(NONE)
    private List<Object> uriVars = new ArrayList<>();

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

    public Request<RESP> uriVars(Object... vars) {
        uriVars.clear();
        uriVars.addAll(asList(vars));
        return this;
    }

    public Object[] getUriVars() {
        return uriVars.toArray();
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
