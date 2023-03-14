package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Patch;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;
import static org.springframework.http.HttpMethod.PATCH;

public class PatchExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Patch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Patch annotation = parameterContext.findAnnotation(Patch.class).get();
        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(PATCH, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), extensionContext));
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
