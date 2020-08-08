package com.cariochi.recordo.verify;

import com.cariochi.recordo.Verify;
import com.cariochi.recordo.json.JsonConverters;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class VerifyParameterResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Verify.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        return new Expected<>(
                parameterContext.findAnnotation(Verify.class).get(),
                JsonConverters.find(extensionContext.getRequiredTestInstance())
        );
    }
}
