package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Delete;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.DELETE;

public class DeleteParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Delete.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Delete annotation = parameter.findAnnotation(Delete.class).get();
        return processRequest(DELETE, annotation.value(), parameter, extension);
    }

}
