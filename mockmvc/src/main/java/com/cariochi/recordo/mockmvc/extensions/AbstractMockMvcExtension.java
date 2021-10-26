package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.Response;
import com.cariochi.reflecto.fields.JavaField;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.core.json.JsonConverters.findJsonConverter;
import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

public abstract class AbstractMockMvcExtension implements ParameterResolver {

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

        final RecordoMockMvc mockMvc = getMockMvcClient(extension);

        Request<Object> request = mockMvc
                .request(method, path, responseType)
                .headers(parseHeaders(headers))
                .expectedStatus(status)
                .body(body);

        request = intercept(interceptors, mockMvc, request);
        return executeRequest(request, parameter);
    }

    @SneakyThrows
    private Request<Object> intercept(Class<? extends RequestInterceptor>[] interceptors,
                                      RecordoMockMvc mockMvc,
                                      Request<Object> request) {
        for (Class<? extends RequestInterceptor> type : interceptors) {
            final RequestInterceptor interceptor = type.getConstructor().newInstance();
            request = interceptor.intercept(request, mockMvc);
        }
        return request;
    }

    protected RecordoMockMvc getMockMvcClient(ExtensionContext context) {
        final MockMvc mockMvc = findMockMvc(context)
                .or(() -> findBean(context, MockMvc.class))
                .orElseThrow();
        final JsonConverter jsonConverter = findJsonConverter(context.getRequiredTestInstance())
                .or(() -> findBean(context, ObjectMapper.class).map(JsonConverter::new))
                .orElseGet(JsonConverter::new);
        return new RecordoMockMvc(mockMvc, jsonConverter);
    }

    public String getBodyFromFile(String bodyFile, Object testInstance) {
        final JsonConverter jsonConverter = getJsonConverter(testInstance);
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        return (String) objectReader.read(bodyFile, String.class);
    }

    private <T> Optional<T> findBean(ExtensionContext extensionContext, Class<T> beanClass) {
        try {
            return Optional.of(getApplicationContext(extensionContext).getBean(beanClass));
        } catch (BeansException e) {
            return Optional.empty();
        }
    }

    private Object executeRequest(Request<Object> request, ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        if (isWaitingForRequest(parameterClass)) {
            return request;
        } else if (isWaitingForResponse(parameterClass)) {
            return request.perform();
        } else {
            return request.perform().getBody();
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

    private Optional<MockMvc> findMockMvc(ExtensionContext context) {
        return reflect(context.getRequiredTestInstance()).fields()
                .withTypeAndAnnotation(MockMvc.class, EnableRecordo.class).stream().findAny()
                .map(JavaField::getValue)
                .map(MockMvc.class::cast);
    }

    private Map<String, String> parseHeaders(String[] headers) {
        final String separator = "=";
        return Stream.of(headers).collect(toMap(
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
