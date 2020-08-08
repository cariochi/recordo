package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.PATCH;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.http.HttpMethod;

public class PatchParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(PATCH.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final PATCH annotation = parameter.findAnnotation(PATCH.class).get();
        return processRequest(HttpMethod.PATCH, annotation.value(), parameter, extension);
    }

}
