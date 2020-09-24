package com.cariochi.recordo.mockhttp.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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
public class Request<RESP> {

    private final MockHttpClient client;

    private final HttpMethod method;

    private final String path;

    private final Type responseType;

    @Setter(NONE)
    private Map<String, String> headers = new LinkedHashMap<>();

    private Object body;

    @Setter(NONE)
    private List<Object> parameters = new ArrayList<>();

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

    public Request<RESP> parameters(Object... parameters) {
        this.parameters.clear();
        this.parameters.addAll(asList(parameters));
        return this;
    }

    public Object[] parameters() {
        return parameters.toArray();
    }
}
