package com.cariochi.recordo.mockserver.interceptors;

import java.io.Closeable;

public interface MockServerInterceptor extends Closeable {

    void init(RecordoRequestHandler handler);

    void close();
}
