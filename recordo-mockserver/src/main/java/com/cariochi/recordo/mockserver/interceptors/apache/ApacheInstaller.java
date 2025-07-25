package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.interceptors.InterceptorInstaller;
import com.cariochi.recordo.mockserver.interceptors.RecordoRequestHandler;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.classic.HttpClient;

public class ApacheInstaller implements InterceptorInstaller {

    private final HttpClient httpClient;
    private ApacheRecordoInterceptor interceptor;
    private ExecChainHandler originalHandler;

    public ApacheInstaller(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ApacheInstaller install(ApacheRecordoInterceptor interceptor) {
        this.interceptor = interceptor;
        this.originalHandler = attach(httpClient, interceptor);
        return this;
    }

    @Override
    public void init(RecordoRequestHandler handler) {
        interceptor.init(handler);
    }

    @Override
    public void uninstall() {
        detach(httpClient, interceptor, originalHandler);
    }

    private ExecChainHandler attach(Object target, ApacheRecordoInterceptor interceptor) {
        if (target == null) {
            return null;
        }

        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler();

        if (handler == null) {
            return null;
        } else {
            execChainElementProxy.setHandler(interceptor);
            return handler;
        }
    }

    private void detach(Object target, ApacheRecordoInterceptor interceptor, ExecChainHandler originalHandler) {
        if (target == null) {
            return;
        }

        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler();

        if (handler == null) {
            return;
        } else if (handler == interceptor) {
            execChainElementProxy.setHandler(originalHandler);
        }

        detach(execChainElementProxy.getExecChainElement(), interceptor, originalHandler);

    }
}
