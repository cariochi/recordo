package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Put;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;
import static org.springframework.http.HttpMethod.PUT;

public class PutExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Put.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Put annotation = parameterContext.findAnnotation(Put.class).get();
        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(PUT, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), extensionContext));
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
