package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.given.GivenFileReader;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class GivenParameterResolver implements ParamResolver {

    private final GivenFileReader givenFileReader = new GivenFileReader();

    @Override
    public boolean supports(RecordoContext context) {
        return context.isAnnotated(Given.class);
    }

    @Override
    public Object resolveParameter(RecordoContext context) {
        final Given given = context.getAnnotation(Given.class);
        final String parameterName =
                Optional.of(given.value()).filter(StringUtils::isNotBlank).orElseGet(context::getParameterName);
        final Object testInstance = context.getTestInstance();
        final String methodName = context.getTestMethod().getName();
        return givenFileReader
                .readFromFile(testInstance, methodName, given.file(), context.getParameterType(), parameterName);
    }

}
