package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Perform;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;

public class PerformExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Perform.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Perform annotation = parameterContext.findAnnotation(Perform.class).get();
        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(annotation.method(), annotation.path(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), extensionContext));
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
