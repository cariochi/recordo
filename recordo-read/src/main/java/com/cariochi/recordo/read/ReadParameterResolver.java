package com.cariochi.recordo.read;

import com.cariochi.recordo.core.RegularExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;

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
                        parameterContext.getParameter(),
                        getJsonConverter(annotation.objectMapper(), extensionContext)
                ))
                .orElse(null);
    }

    private Object resolveParameter(String fileName, Parameter parameter, JsonConverter jsonConverter) {
        final Type parameterType = parameter.getParameterizedType();
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        if (ObjectFactory.class.isAssignableFrom(parameter.getType())) {
            final Type actualTypeArgument = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            return new ObjectFactory<>(objectReader, fileName, actualTypeArgument);
        } else if (ObjectTemplate.class.isAssignableFrom(parameter.getType())) {
            final Type actualTypeArgument = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            return new ObjectTemplate<>(objectReader, fileName, actualTypeArgument);
        } else {
            return objectReader.read(fileName, parameterType);
        }
    }

}
