package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.Verify;
import com.cariochi.recordo.verify.Expected;

public class VerifyParameterResolver implements ParamResolver {

    @Override
    public boolean supports(RecordoContext context) {
        return context.isAnnotated(Verify.class);
    }

    @Override
    public Object resolveParameter(RecordoContext context) {
        final Verify verify = context.getAnnotation(Verify.class);
        return Expected.builder()
                .annotation(verify)
                .testInstance(context.getTestInstance())
                .build();
    }
}
