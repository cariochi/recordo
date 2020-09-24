package com.cariochi.recordo.mockhttp.client.resolvers;

import com.cariochi.recordo.mockhttp.client.MockHttpPatch;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.PATCH;

public class PatchParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(MockHttpPatch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final MockHttpPatch annotation = parameter.findAnnotation(MockHttpPatch.class).get();
        return processRequest(
                PATCH,
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
