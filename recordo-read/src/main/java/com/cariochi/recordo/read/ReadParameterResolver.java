package com.cariochi.recordo.read;

import com.cariochi.recordo.core.RegularExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.parameters.ReflectoParameter;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.reflecto.Reflecto.reflect;

public class ReadParameterResolver implements RegularExtension, ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Read.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.findAnnotation(Read.class)
                .map(annotation -> resolveParameter(
                        annotation.value(),
                        reflect(parameterContext.getParameter()),
                        getJsonConverter(annotation.objectMapper(), extensionContext)
                ))
                .orElse(null);
    }

    private Object resolveParameter(String fileName, ReflectoParameter parameter, JsonConverter jsonConverter) {
        final ReflectoType type = parameter.type();
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        if (type.is(ObjectFactory.class)) {
            final ReflectoType typeArgument = type.arguments().get(0);
            return new ObjectFactory<>(objectReader, fileName, typeArgument);
        } else {
            return objectReader.read(fileName, type);
        }
    }

}
