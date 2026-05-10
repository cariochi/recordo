package com.cariochi.recordo.mockserver.interceptors;

import java.io.Closeable;
import java.util.Optional;

public interface InterceptorInstaller<T extends RecordoInterceptor> extends Closeable {

    InterceptorInstaller<T> install(T interceptor);

    void setHandler(RecordoRequestHandler handler);

    Optional<RecordoInterceptor> findInterceptor();

    void uninstall();

    default void close() {
        uninstall();
    }
}
