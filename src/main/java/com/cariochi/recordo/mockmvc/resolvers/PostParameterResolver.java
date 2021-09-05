package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Post;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.POST;

public class PostParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Post.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Post annotation = parameter.findAnnotation(Post.class).get();
        return processRequest(
                POST,
                annotation.value(),
                annotation.headers(),
                annotation.body(),
                annotation.expectedStatus(),
                annotation.interceptors(),
                parameter,
                extension);
    }

}
