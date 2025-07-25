package com.cariochi.recordo.mockserver.interceptors;

import java.io.Closeable;

public interface InterceptorInstaller extends Closeable {

    void init(RecordoRequestHandler handler);

    void uninstall();

    default void close() {
        uninstall();
    }
}
