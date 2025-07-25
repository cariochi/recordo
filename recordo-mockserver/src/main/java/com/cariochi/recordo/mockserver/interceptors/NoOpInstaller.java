package com.cariochi.recordo.mockserver.interceptors;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoOpInstaller implements InterceptorInstaller {

    private final RecordoInterceptor interceptor;

    @Override
    public void init(RecordoRequestHandler handler) {
        interceptor.init(handler);
    }

    @Override
    public void uninstall() {

    }
}
