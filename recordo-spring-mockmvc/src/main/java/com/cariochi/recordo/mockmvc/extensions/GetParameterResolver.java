package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockmvc.Get;
import com.cariochi.recordo.mockmvc.Request;
import java.lang.reflect.Type;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.createRecordoMockMvc;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static org.springframework.http.HttpMethod.GET;

public class GetParameterResolver implements MockMvcParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameter.isAnnotated(Get.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Get annotation = parameterContext.findAnnotation(Get.class).orElseThrow();
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), extensionContext);
        final Type type = parameterContext.getParameter().getParameterizedType();
        final Request<Object> request = createRecordoMockMvc(extensionContext, jsonConverter)
                .request(GET, annotation.value(), getResponseType(type))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus());
        return processRequest(request, type, annotation.interceptors());
    }

}
