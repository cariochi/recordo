package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.Delete;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import java.lang.reflect.Type;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static org.springframework.http.HttpMethod.DELETE;

public class DeleteParameterResolver implements MockMvcParameterResolver {

    private final RecordoMockMvcCreator recordoMockMvcCreator = new RecordoMockMvcCreator();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Delete.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Delete annotation = parameterContext.findAnnotation(Delete.class).orElseThrow();
        final Type type = parameterContext.getParameter().getParameterizedType();
        final RecordoMockMvc recordoMockMvc = recordoMockMvcCreator.create(annotation.objectMapper(), extensionContext);
        final Request<Object> request = recordoMockMvc
                .request(DELETE, annotation.value(), getResponseType(type))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus());
        return processRequest(request, type, annotation.interceptors());
    }

}
