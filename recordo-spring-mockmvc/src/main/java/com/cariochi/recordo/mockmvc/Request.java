package com.cariochi.recordo.mockmvc;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static lombok.AccessLevel.NONE;
import static org.apache.commons.lang3.ArrayUtils.addFirst;

@Data
@Accessors(fluent = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Request<RESP> {

    @Getter
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

    private List<File> files = new ArrayList<>();

    private HttpStatus expectedStatus;

    public Response<RESP> perform() {
        return client.perform(this);
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

    public Request<RESP> uriVars(Object[] pathVars) {
        uriVars = pathVars;
        return this;
    }

    public Request<RESP> params(MultiValueMap<String, String> params) {
        params.forEach(this::addToParams);
        return this;
    }

    public Request<RESP> param(String name, String... values) {
        addToParams(name, List.of(values));
        return this;
    }

    public Request<RESP> file(File file) {
        files.add(file);
        return this;
    }

    private void addToParams(String name, List<String> values) {
        values.forEach(value -> params.add(name, value));
    }

    @Value
    @Builder
    public static class File {

        String name;
        String originalFilename;
        String contentType;
        byte[] content;

    }

}
