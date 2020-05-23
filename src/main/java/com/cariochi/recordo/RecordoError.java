package com.cariochi.recordo;

public class RecordoError extends RuntimeException {

    public RecordoError(String message) {
        super(message);
    }

    public RecordoError(Throwable e) {
        super(e);
    }
}
