package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.DELETE;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.http.HttpMethod;

public class DeleteParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(DELETE.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final DELETE annotation = parameter.findAnnotation(DELETE.class).get();
        return processRequest(HttpMethod.DELETE, annotation.value(), parameter, extension);
    }

}
