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
        ExecChainHandler handler = execChainElementProxy.getHandler();

        if (handler == null) {
            return null;
        } else if (isAssignableFrom(handler, OnRequestExecChain.class)) {
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

        if (target == null) {
            return null;
        }

        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler();

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

    public static void detachExecChain(Object target, AbstractExecChainHandler chainHandler) {

        if (target == null) {
            return;
        }

        ExecChainElementProxy execChainElementProxy = ExecChainElementProxy.create(target);
        ExecChainHandler handler = execChainElementProxy.getHandler();

        if (handler == null) {
            return;
        } else if (handler == chainHandler) {
            final ExecChainHandler originalHandler = chainHandler.getExecChainHandler();
            execChainElementProxy.setHandler(originalHandler);
        }

        detachExecChain(execChainElementProxy.getExecChainElement(), chainHandler);

    }

    private boolean isAssignableFrom(Object target, Class<?> clazz) {
        return target != null && clazz.isAssignableFrom(target.getClass());
    }

}
