package com.cariochi.recordo.mockhttp;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class MockHttpParameterResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameter.isAnnotated(MockHttp.class)
               && parameter.getParameter().getType().isAssignableFrom(MockHttpServer.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        return parameter.findAnnotation(MockHttp.class)
                .map(MockHttp::value)
                .map(file -> new MockHttpServer(file, extension.getRequiredTestInstance()))
                .orElse(null);
    }
}
