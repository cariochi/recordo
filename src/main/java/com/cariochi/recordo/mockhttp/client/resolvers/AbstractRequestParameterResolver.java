package com.cariochi.recordo.mockhttp.client.resolvers;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.mockhttp.client.MockHttpClient;
import com.cariochi.recordo.mockhttp.client.Request;
import com.cariochi.recordo.mockhttp.client.RequestInterceptor;
import com.cariochi.recordo.mockhttp.client.Response;
import com.cariochi.recordo.read.ObjectReader;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public abstract class AbstractRequestParameterResolver implements ParameterResolver {

    protected Object processRequest(HttpMethod method,
                                    String path,
                                    String[] headers,
                                    String bodyFile,
                                    HttpStatus status,
                                    Class<? extends RequestInterceptor>[] interceptors,
                                    ParameterContext parameter,
                                    ExtensionContext extension) {

        final String body = Optional.ofNullable(bodyFile)
                .filter(StringUtils::isNotBlank)
                .map(file -> getBodyFromFile(file, extension.getRequiredTestInstance()))
                .orElse(null);

        final Type responseType = getResponseType(parameter);

        final MockHttpClient mockHttpClient = getMockMvcClient(extension);

        Request<Object> request = mockHttpClient
                .request(method, path, responseType)
                .headers(parseHeaders(headers))
                .expectedStatus(status)
                .body(body);

        request = intercept(interceptors, mockHttpClient, request);
        return executeRequest(request, parameter);
    }

    @SneakyThrows
    private Request<Object> intercept(Class<? extends RequestInterceptor>[] interceptors,
                                      MockHttpClient mockHttpClient,
                                      Request<Object> request) {
        for (Class<? extends RequestInterceptor> type : interceptors) {
            final RequestInterceptor interceptor = type.getConstructor().newInstance();
            request = interceptor.intercept(request, mockHttpClient);
        }
        return request;
    }

    protected MockHttpClient getMockMvcClient(ExtensionContext extension) {
        final Object testInstance = extension.getRequiredTestInstance();
        return new MockHttpClient(JsonConverters.find(testInstance), mockMvc(testInstance));
    }

    public String getBodyFromFile(String bodyFile, Object testInstance) {
        final JsonConverter jsonConverter = JsonConverters.find(testInstance);
        return (String) ObjectReader.read(bodyFile, String.class, jsonConverter);
    }

    private Object executeRequest(Request<Object> request, ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        if (isWaitingForRequest(parameterClass)) {
            return request;
        } else if (isWaitingForResponse(parameterClass)) {
            return request.execute();
        } else {
            return request.execute().getBody();
        }
    }

    private Type getResponseType(ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        final Type parameterType = parameter.getParameter().getParameterizedType();
        if (isWaitingForRequest(parameterClass)) {
            return ((ParameterizedType) parameterType).getActualTypeArguments()[0];
        } else if (isWaitingForResponse(parameterClass)) {
            return ((ParameterizedType) parameterType).getActualTypeArguments()[0];
        } else {
            return parameterType;
        }
    }

    private MockMvc mockMvc(Object testInstance) {
        return Fields.of(testInstance)
                .withTypeAndAnnotation(MockMvc.class, EnableRecordo.class).stream().findAny()
                .map(TargetField::getValue)
                .map(MockMvc.class::cast)
                .orElse(null);
    }

    private Map<String, String> parseHeaders(String[] headers) {
        final String separator = "=";
        return stream(headers).collect(toMap(
                h -> substringBefore(h, separator).trim(),
                h -> substringAfter(h, separator).trim()
        ));
    }

    private boolean isWaitingForRequest(Class<?> type) {
        return Request.class.isAssignableFrom(type);
    }

    private boolean isWaitingForResponse(Class<?> type) {
        return Response.class.isAssignableFrom(type);
    }

}
