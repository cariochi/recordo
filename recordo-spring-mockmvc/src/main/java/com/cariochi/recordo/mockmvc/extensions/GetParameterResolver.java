package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Get;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import java.lang.reflect.Type;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static org.springframework.http.HttpMethod.GET;

public class GetParameterResolver implements MockMvcParameterResolver {

    private final RecordoMockMvcCreator recordoMockMvcCreator = new RecordoMockMvcCreator();

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameter.isAnnotated(Get.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Get annotation = parameterContext.findAnnotation(Get.class).orElseThrow();
        final Type type = parameterContext.getParameter().getParameterizedType();
        final RecordoMockMvc recordoMockMvc = recordoMockMvcCreator.create(annotation.objectMapper(), extensionContext);
        final Request<Object> request = recordoMockMvc
                .request(GET, annotation.value(), getResponseType(type))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus());
        return processRequest(request, type, annotation.interceptors());
    }

}
