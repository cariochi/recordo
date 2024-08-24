package com.cariochi.recordo.mockserver.interceptors.apache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.ExecChainHandler;

@RequiredArgsConstructor
public abstract class AbstractExecChainHandler implements ExecChainHandler {

    @Getter
    private final ExecChainHandler execChainHandler;

    protected final ApacheMapper mapper = new ApacheMapper();

}
