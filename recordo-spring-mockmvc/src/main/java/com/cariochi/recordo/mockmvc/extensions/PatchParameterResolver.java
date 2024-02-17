package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockmvc.Patch;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getBody;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static com.cariochi.reflecto.Reflecto.reflect;
import static org.springframework.http.HttpMethod.PATCH;

public class PatchParameterResolver implements MockMvcParameterResolver {

    private final RecordoMockMvcCreator recordoMockMvcCreator = new RecordoMockMvcCreator();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Patch.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Patch annotation = parameterContext.findAnnotation(Patch.class).orElseThrow();
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), extensionContext);
        final ReflectoType type = reflect(parameterContext.getParameter()).type();
        final RecordoMockMvc recordoMockMvc = recordoMockMvcCreator.create(annotation.objectMapper(), extensionContext);
        final Request<Object> request = recordoMockMvc
                .request(PATCH, annotation.value(), getResponseType(type).actualType())
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), jsonConverter));
        return processRequest(request, type, annotation.interceptors());
    }

}
