package com.cariochi.recordo.httpmock.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecordoRequest {
    private String method;
    private String url;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Object body;

    public String getUrl() {
        return url;
    }

    public RecordoRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RecordoRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RecordoRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public RecordoRequest setBody(Object body) {
        this.body = body;
        return this;
    }
}
