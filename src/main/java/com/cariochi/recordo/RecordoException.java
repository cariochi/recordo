package com.cariochi.recordo;

public class RecordoException  extends RuntimeException {

    public RecordoException(String message) {
        super(message);
    }

    public RecordoException(Throwable e) {
        super(e);
    }
}
