package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.Get;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static org.springframework.http.HttpMethod.GET;

public class GetParameterResolver extends AbstractRequestParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameter.isAnnotated(Get.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        final Get annotation = parameter.findAnnotation(Get.class).get();
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
