package com.cariochi.recordo.mockhttp.client.resolvers;

import com.cariochi.recordo.mockhttp.client.MockHttpGet;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.GET;

public class GetParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameter.isAnnotated(MockHttpGet.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final MockHttpGet annotation = parameter.findAnnotation(MockHttpGet.class).get();
        return processRequest(
                GET,
                annotation.value(),
                annotation.headers(),
                null,
                annotation.expectedStatus(),
                annotation.interceptors(),
                parameter,
                extension
        );
    }

}
