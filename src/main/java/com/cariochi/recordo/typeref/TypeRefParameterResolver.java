package com.cariochi.recordo.typeref;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeRefParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return TypeRef.class.isAssignableFrom(parameter.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return new TypeRef<>(getType(parameter));
    }

    private Type getType(ParameterContext parameter) {
        final ParameterizedType parameterType = (ParameterizedType) parameter.getParameter().getParameterizedType();
        return parameterType.getActualTypeArguments()[0];
    }

}
