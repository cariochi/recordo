package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Put;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.PUT;

public class PutParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Put.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Put annotation = parameter.findAnnotation(Put.class).get();
        return processRequest(
                PUT,
                annotation.value(),
                annotation.headers(),
                annotation.body(),
                annotation.expectedStatus(),
                annotation.interceptors(),
                parameter,
                extension
        );
    }

}
