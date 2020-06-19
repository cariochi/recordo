package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.utils.Files;
import com.cariochi.recordo.verify.Verifier;

public class VerifyParameterResolver implements ParamResolver {

    private final Files files = new Files();

    @Override
    public boolean supports(RecordoContext context) {
        return context.isAnnotated(Verify.class);
    }

    @Override
    public Object resolveParameter(RecordoContext context) {
        final Verify verify = context.getAnnotation(Verify.class);
        return Verifier.builder()
                .files(files)
                .annotation(verify)
                .testInstance(context.getTestInstance())
                .testMethod(context.getTestMethod())
                .parameterName(context.getParameterName())
                .build();
    }
}
