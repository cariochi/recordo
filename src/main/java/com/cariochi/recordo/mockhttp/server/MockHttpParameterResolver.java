package com.cariochi.recordo.mockhttp.server;

import com.cariochi.recordo.MockHttpServer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class MockHttpParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameter.isAnnotated(MockHttpServer.class)
               && parameter.getParameter().getType().isAssignableFrom(com.cariochi.recordo.mockhttp.server.MockHttpServer.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        return parameter.findAnnotation(MockHttpServer.class)
                .map(MockHttpServer::value)
                .map(file -> new com.cariochi.recordo.mockhttp.server.MockHttpServer(file, extension.getRequiredTestInstance()))
                .orElse(null);
    }
}
