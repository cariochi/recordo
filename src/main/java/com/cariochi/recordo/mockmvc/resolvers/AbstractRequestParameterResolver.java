package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.given.GivenObjectReader;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockmvc.Body;
import com.cariochi.recordo.mockmvc.Headers;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Response;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public abstract class AbstractRequestParameterResolver implements ParameterResolver {

    protected Object processRequest(HttpMethod method,
                                    String path,
                                    ParameterContext parameter,
                                    ExtensionContext extension) {
        final Object testInstance = extension.getRequiredTestInstance();
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final MockMvc mockMvc = mockMvc(testInstance);
        final Map<String, String> headers = parameter.findAnnotation(Headers.class)
                .map(this::headers)
                .orElse(Collections.emptyMap());
        final String body = parameter.findAnnotation(Body.class)
                .map(annotation -> getBodyFromFile(annotation, extension.getRequiredTestInstance()))
                .orElse(null);
        final Request.RequestBuilder<Object> requestBuilder = Request.builder()
                .mockMvc(mockMvc)
                .jsonConverter(jsonConverter)
                .method(method)
                .path(path)
                .headers(headers)
                .body(body);
        return executeRequest(requestBuilder, parameter);
    }

    public String getBodyFromFile(Body annotation, Object testInstance) {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        return  GivenObjectReader.read(annotation.value(), String.class, jsonConverter);
    }

    private Object executeRequest(Request.RequestBuilder<Object> requestBuilder, ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        final Type parameterType = parameter.getParameter().getParameterizedType();
        if (isWaitingForRequest(parameterClass)) {
            final Type responseType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            return requestBuilder.responseType(responseType).build();
        } else if (isWaitingForResponse(parameterClass)) {
            final Type responseType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            return requestBuilder.responseType(responseType).build().execute();
        } else {
            return requestBuilder.responseType(parameterType).build().execute().getBody();
        }
    }

    private MockMvc mockMvc(Object testInstance) {
        return Fields.of(testInstance)
                .withTypeAndAnnotation(MockMvc.class, EnableRecordo.class).stream().findAny()
                .map(TargetField::getValue)
                .map(MockMvc.class::cast)
                .orElse(null);
    }

    private Map<String, String> headers(Headers headers) {
        return stream(headers.value()).collect(toMap(
                h -> substringBefore(h, ":").trim(),
                h -> substringAfter(h, ":").trim()
        ));
    }

    private boolean isWaitingForRequest(Class<?> type) {
        return Request.class.isAssignableFrom(type);
    }

    private boolean isWaitingForResponse(Class<?> type) {
        return Response.class.isAssignableFrom(type);
    }

}
