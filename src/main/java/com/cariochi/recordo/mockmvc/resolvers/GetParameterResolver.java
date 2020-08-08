package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.GET;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.http.HttpMethod;

public class GetParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(GET.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final GET annotation = parameter.findAnnotation(GET.class).get();
        return processRequest(HttpMethod.GET, annotation.value(), parameter, extension);
    }

}
