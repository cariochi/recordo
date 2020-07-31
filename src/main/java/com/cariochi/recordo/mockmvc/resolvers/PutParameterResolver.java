package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.PUT;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.http.HttpMethod;

public class PutParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(PUT.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final PUT annotation = parameter.findAnnotation(PUT.class).get();
        return processRequest(HttpMethod.PUT, annotation.value(), parameter, extension);
    }

}
