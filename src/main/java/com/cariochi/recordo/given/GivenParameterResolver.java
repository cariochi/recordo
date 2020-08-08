package com.cariochi.recordo.given;

import com.cariochi.recordo.Given;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Type;

public class GivenParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Given.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.findAnnotation(Given.class)
                .map(annotation -> {
                    final Type parameterType = parameterContext.getParameter().getParameterizedType();
                    final JsonConverter jsonConverter = JsonConverters.find(extensionContext.getRequiredTestInstance());
                    return new GivenObjectProvider(jsonConverter).get(annotation.value(), parameterType);
                })
                .orElse(null);
    }
}
