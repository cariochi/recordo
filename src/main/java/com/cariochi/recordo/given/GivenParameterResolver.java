package com.cariochi.recordo.given;

import com.cariochi.recordo.Given;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
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
                .map(Given::value)
                .map(fileName -> resolveParameter(
                        fileName,
                        parameterContext.getParameter(),
                        JsonConverters.find(extensionContext.getRequiredTestInstance())
                ))
                .orElse(null);
    }

    private Object resolveParameter(String fileName, Parameter parameter, JsonConverter jsonConverter) {
        if (isExpected(parameter)) {
            return new Assertion<>(fileName, jsonConverter);
        } else {
            final Type parameterType = parameter.getParameterizedType();
            return GivenObjectReader.read(fileName, parameterType, jsonConverter);
        }
    }

    private boolean isExpected(Parameter parameter) {
        return Assertion.class.isAssignableFrom(parameter.getType());
    }
}
