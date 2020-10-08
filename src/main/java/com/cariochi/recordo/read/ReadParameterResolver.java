package com.cariochi.recordo.read;

import com.cariochi.recordo.Read;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ReadParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Read.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.findAnnotation(Read.class)
                .map(Read::value)
                .map(fileName -> resolveParameter(
                        fileName,
                        parameterContext.getParameter(),
                        JsonConverters.find(extensionContext.getRequiredTestInstance())
                ))
                .orElse(null);
    }

    private Object resolveParameter(String fileName, Parameter parameter, JsonConverter jsonConverter) {
        final Type parameterType = parameter.getParameterizedType();
        return ObjectReader.read(fileName, parameterType, jsonConverter);
    }

}
