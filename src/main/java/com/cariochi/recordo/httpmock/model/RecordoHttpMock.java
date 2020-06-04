package com.cariochi.recordo.httpmock.model;

public class RecordoHttpMock {

    private RecordoRequest request;
    private RecordoResponse response;

    public RecordoHttpMock() {
    }

    public RecordoHttpMock(RecordoRequest request, RecordoResponse response) {
        this.request = request;
        this.response = response;
    }

    public RecordoRequest getRequest() {
        return request;
    }

    public RecordoHttpMock setRequest(RecordoRequest request) {
        this.request = request;
        return this;
    }

    public RecordoResponse getResponse() {
        return response;
    }

    public RecordoHttpMock setResponse(RecordoResponse response) {
        this.response = response;
        return this;
    }
}
