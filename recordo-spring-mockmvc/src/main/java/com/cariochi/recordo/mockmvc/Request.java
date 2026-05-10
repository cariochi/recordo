package com.cariochi.recordo.mockmvc;

import lombok.*;
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

import static lombok.AccessLevel.NONE;
import static org.apache.commons.lang3.ArrayUtils.addFirst;

/**
 * Mutable MockMvc request prepared by Recordo.
 * <p>
 * A {@code Request} can be returned from a typed API client method when the test needs to add headers,
 * query parameters, path variables, body, or multipart files before explicitly calling {@link #perform()}.
 *
 * @param <RESP> expected response body type
 */
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

    /**
     * Performs this request through the owning {@link RecordoMockMvc} instance.
     *
     * @return response with status, headers, and deserialized body
     */
    public Response<RESP> perform() {
        return client.perform(this);
    }

    /**
     * Replaces all request headers.
     */
    public Request<RESP> headers(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Adds or replaces one request header.
     */
    public Request<RESP> header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Sets URI variable values used to expand path templates.
     */
    public Request<RESP> uriVars(Object pathVar, Object... pathVars) {
        uriVars = addFirst(pathVars, pathVar);
        return this;
    }

    /**
     * Sets URI variable values used to expand path templates.
     */
    public Request<RESP> uriVars(Object[] pathVars) {
        uriVars = pathVars;
        return this;
    }

    /**
     * Adds request query parameters.
     */
    public Request<RESP> params(MultiValueMap<String, String> params) {
        params.forEach(this::addToParams);
        return this;
    }

    /**
     * Adds one request query parameter with one or more values.
     */
    public Request<RESP> param(String name, String... values) {
        addToParams(name, List.of(values));
        return this;
    }

    /**
     * Adds one multipart file to this request.
     */
    public Request<RESP> file(File file) {
        files.add(file);
        return this;
    }

    private void addToParams(String name, List<String> values) {
        values.forEach(value -> params.add(name, value));
    }

    /**
     * Multipart file attached to a MockMvc request.
     */
    @Value
    @Builder
    public static class File {

        String name;
        String originalFilename;
        String contentType;
        byte[] content;

    }

}
