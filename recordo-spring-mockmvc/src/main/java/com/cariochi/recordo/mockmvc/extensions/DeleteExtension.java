package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Delete;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;
import static org.springframework.http.HttpMethod.DELETE;

public class DeleteExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Delete.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Delete annotation = parameterContext.findAnnotation(Delete.class).get();
        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(DELETE, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus());
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
