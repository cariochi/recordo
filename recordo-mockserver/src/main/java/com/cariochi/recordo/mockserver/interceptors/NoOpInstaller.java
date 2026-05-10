package com.cariochi.recordo.mockserver.interceptors;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class NoOpInstaller implements InterceptorInstaller<RecordoInterceptor> {

    private final RecordoInterceptor interceptor;

    @Override
    public NoOpInstaller install(RecordoInterceptor interceptor) {
        return this;
    }

    @Override
    public void setHandler(RecordoRequestHandler handler) {
        interceptor.setHandler(handler);
    }

    @Override
    public Optional<RecordoInterceptor> findInterceptor() {
        return Optional.of(interceptor);
    }

    @Override
    public void uninstall() {
        interceptor.setHandler(null);
    }
}
