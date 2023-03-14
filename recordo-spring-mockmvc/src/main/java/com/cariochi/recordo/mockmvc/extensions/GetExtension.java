package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Get;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;
import static org.springframework.http.HttpMethod.GET;

public class GetExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameter.isAnnotated(Get.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Get annotation = parameterContext.findAnnotation(Get.class).get();
        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(GET, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus());
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
