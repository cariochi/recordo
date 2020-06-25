package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.Given;
import com.cariochi.recordo.given.GivenObject;

public class GivenParameterResolver implements ParamResolver {

    @Override
    public boolean supports(RecordoContext context) {
        return context.isAnnotated(Given.class);
    }

    @Override
    public Object resolveParameter(RecordoContext context) {
        final Given given = context.getAnnotation(Given.class);
        final Object testInstance = context.getTestInstance();
        return GivenObject.builder()
                .testInstance(testInstance)
                .file(given.value())
                .parameterType(context.getParameterType())
                .build()
                .get();
    }

}
