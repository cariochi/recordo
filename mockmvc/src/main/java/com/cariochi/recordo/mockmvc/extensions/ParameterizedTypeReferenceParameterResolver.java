package com.cariochi.recordo.mockmvc.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeReferenceParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return ParameterizedTypeReference.class.isAssignableFrom(parameter.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return ParameterizedTypeReference.forType(getType(parameter));
    }

    private Type getType(ParameterContext parameter) {
        final ParameterizedType parameterType = (ParameterizedType) parameter.getParameter().getParameterizedType();
        return parameterType.getActualTypeArguments()[0];
    }

}
