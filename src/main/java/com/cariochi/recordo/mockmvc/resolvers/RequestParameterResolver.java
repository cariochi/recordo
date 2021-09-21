package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Perform;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

public class RequestParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Perform.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Perform annotation = parameter.findAnnotation(Perform.class).get();
        return processRequest(
                annotation.method(),
                annotation.path(),
                annotation.headers(),
                annotation.body(),
                annotation.expectedStatus(),
                annotation.interceptors(),
                parameter,
                extension);
    }

}
