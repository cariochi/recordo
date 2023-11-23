package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockmvc.Put;
import com.cariochi.recordo.mockmvc.Request;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getBody;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getMockMvcClient;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static org.springframework.http.HttpMethod.PUT;

public class PutExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Put.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Put annotation = parameterContext.findAnnotation(Put.class).orElseThrow();
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), extensionContext);
        final Request<Object> request = getMockMvcClient(extensionContext, jsonConverter)
                .request(PUT, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), jsonConverter));
        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

}
