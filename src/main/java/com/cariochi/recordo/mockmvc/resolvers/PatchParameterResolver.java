package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Patch;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.PATCH;

public class PatchParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Patch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Patch annotation = parameter.findAnnotation(Patch.class).get();
        return processRequest(PATCH, annotation.value(), parameter, extension);
    }

}
