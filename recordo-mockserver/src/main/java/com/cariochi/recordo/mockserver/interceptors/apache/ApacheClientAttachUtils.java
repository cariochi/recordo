package com.cariochi.recordo.mockserver.interceptors.apache;

import lombok.experimental.UtilityClass;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.impl.classic.MainClientExec;

@UtilityClass
public class ApacheClientAttachUtils {

    public static OnRequestExecChain attachOnRequestExecChain(Object target) {
        if (target == null) {
            return null;
        }

        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler().orElse(null);
        if (isAssignableFrom(handler, OnRequestExecChain.class)) {
            return (OnRequestExecChain) handler;
        } else if (isAssignableFrom(handler, MainClientExec.class)) {
            final OnRequestExecChain onRequestExecChain = new OnRequestExecChain(handler);
            execChainElementProxy.setHandler(onRequestExecChain);
            return onRequestExecChain;
        } else {
            return attachOnRequestExecChain(execChainElementProxy.getExecChainElement());
        }
    }

    public static OnResponseExecChain attachOnResponseExecChain(Object target) {
        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler().orElse(null);

        if (handler == null) {
            return null;
        } else if (isAssignableFrom(handler, OnResponseExecChain.class)) {
            return (OnResponseExecChain) handler;
        } else {
            final OnResponseExecChain onResponseExecChain = new OnResponseExecChain(handler);
            execChainElementProxy.setHandler(onResponseExecChain);
            return onResponseExecChain;
        }
    }

    private boolean isAssignableFrom(Object target, Class<?> clazz) {
        return target != null && clazz.isAssignableFrom(target.getClass());
    }

}
