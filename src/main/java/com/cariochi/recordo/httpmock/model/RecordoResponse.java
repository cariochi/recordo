package com.cariochi.recordo.httpmock.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class RecordoResponse {
    private String protocol;
    private Integer statusCode;
    private String statusText;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Object body;

    public String getProtocol() {
        return protocol;
    }

    public RecordoResponse setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public RecordoResponse setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getStatusText() {
        return statusText;
    }

    public RecordoResponse setStatusText(String statusText) {
        this.statusText = statusText;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RecordoResponse setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public RecordoResponse setBody(Object body) {
        this.body = body;
        return this;
    }
}
