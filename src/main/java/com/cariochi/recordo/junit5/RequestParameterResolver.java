package com.cariochi.recordo.junit5;

import com.cariochi.recordo.annotation.EnableRecordo;
import com.cariochi.recordo.given.GivenObject;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Response;
import com.cariochi.recordo.mockmvc.annotations.*;
import com.cariochi.recordo.reflection.Fields;
import com.cariochi.recordo.reflection.TargetField;
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
import static org.springframework.http.HttpMethod.*;

public class RequestParameterResolver implements ParamResolver {

    @Override
    public boolean supports(RecordoContext context) {
        return context.isAnnotated(Get.class)
               || context.isAnnotated(Post.class)
               || context.isAnnotated(Put.class)
               || context.isAnnotated(Patch.class)
               || context.isAnnotated(Delete.class);
    }

    @Override
    public Object resolveParameter(RecordoContext context) {
        if (context.isAnnotated(Get.class)) {
            return processRequest(GET, context.getAnnotation(Get.class).value(), context);
        } else if (context.isAnnotated(Post.class)) {
            return processRequest(POST, context.getAnnotation(Post.class).value(), context);
        } else if (context.isAnnotated(Put.class)) {
            return processRequest(PUT, context.getAnnotation(Put.class).value(), context);
        } else if (context.isAnnotated(Patch.class)) {
            return processRequest(PATCH, context.getAnnotation(Patch.class).value(), context);
        } else if (context.isAnnotated(Delete.class)) {
            return processRequest(HttpMethod.DELETE, context.getAnnotation(Delete.class).value(), context);
        } else {
            return null;
        }
    }

    private Object processRequest(HttpMethod method, String path, RecordoContext context) {
        final Object testInstance = context.getTestInstance();
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        final MockMvc mockMvc = mockMvc(testInstance);
        final Map<String, String> headers = context.findAnnotation(Headers.class)
                .map(this::headers)
                .orElse(Collections.emptyMap());
        final String body = context.findAnnotation(Body.class)
                .map(annotation -> getBodyFromFile(annotation, context))
                .orElse(null);
        final Request.RequestBuilder<Object> requestBuilder = Request.builder()
                .mockMvc(mockMvc)
                .jsonConverter(jsonConverter)
                .method(method)
                .path(path)
                .headers(headers)
                .body(body);
        return executeRequest(requestBuilder, context);
    }

    public String getBodyFromFile(Body annotation, RecordoContext context) {
        final Object testInstance = context.getTestInstance();
        return (String) GivenObject.builder()
                .testInstance(testInstance)
                .file(annotation.value())
                .parameterType(String.class)
                .build()
                .get();
    }

    private Object executeRequest(Request.RequestBuilder<Object> requestBuilder, RecordoContext context) {
        if (isWaitingForRequest(context.getParameterClass())) {
            final Type responseType = ((ParameterizedType) context.getParameterType()).getActualTypeArguments()[0];
            return requestBuilder.responseType(responseType).build();
        } else if (isWaitingForResponse(context.getParameterClass())) {
            final Type responseType = ((ParameterizedType) context.getParameterType()).getActualTypeArguments()[0];
            return requestBuilder.responseType(responseType).build().execute();
        } else {
            return requestBuilder.responseType(context.getParameterType()).build().execute().getBody();
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
