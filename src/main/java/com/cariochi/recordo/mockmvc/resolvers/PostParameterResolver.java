package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.POST;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.http.HttpMethod;

public class PostParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(POST.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final POST annotation = parameter.findAnnotation(POST.class).get();
        return processRequest(HttpMethod.POST, annotation.value(), parameter, extension);
    }

}
