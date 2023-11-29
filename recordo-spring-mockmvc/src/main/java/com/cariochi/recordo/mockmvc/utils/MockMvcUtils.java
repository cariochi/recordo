package com.cariochi.recordo.mockmvc.utils;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.recordo.mockmvc.Content;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.Response;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@UtilityClass
public class MockMvcUtils {

    public static Map<String, String> parseHeaders(String[] headers) {
        final String separator = "=";
        return Stream.of(headers).collect(toMap(
                h -> substringBefore(h, separator).trim(),
                h -> substringAfter(h, separator).trim()
        ));
    }

    public static String getBody(Content content, JsonConverter jsonConverter) {
        return Optional.ofNullable(content.value())
                .filter(StringUtils::isNotBlank)
                .or(() -> Optional.ofNullable(content.file())
                        .filter(StringUtils::isNotBlank)
                        .map(file -> getBodyFromFile(file, jsonConverter))
                )
                .orElse(null);
    }

    public static RecordoMockMvc createRecordoMockMvc(ExtensionContext context, JsonConverter jsonConverter) {
        final Collection<RequestInterceptor> requestInterceptors = Beans.of(context).findAll(RequestInterceptor.class).values();
        return createRecordoMockMvc(context, jsonConverter, requestInterceptors);
    }

    public static RecordoMockMvc createRecordoMockMvc(ExtensionContext context, JsonConverter jsonConverter, Collection<RequestInterceptor> requestInterceptors) {
        final MockMvc mockMvc = Beans.of(context).findByType(MockMvc.class)
                .map(MockMvc.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Can't find single instance of MockMvc"));
        return new RecordoMockMvc(mockMvc, jsonConverter, requestInterceptors);
    }

    public static Type getResponseType(Type type) {
        if (isRequestType(type)) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        } else if (isResponseType(type)) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        } else {
            return type;
        }
    }

    public static Object getResponse(Request<Object> request, Type responseType) {
        if (isRequestType(responseType)) {
            return request;
        } else if (isResponseType(responseType)) {
            return request.perform();
        } else {
            return request.perform().getBody();
        }
    }

    public static boolean isRequestType(Type type) {
        return Request.class.isAssignableFrom(getClassFromType(type));
    }

    public static boolean isResponseType(Type type) {
        return Response.class.isAssignableFrom(getClassFromType(type));
    }

    private static String getBodyFromFile(String bodyFile, JsonConverter jsonConverter) {
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        return (String) objectReader.read(bodyFile, String.class);
    }

    private static Class<?> getClassFromType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getClassFromType(((ParameterizedType) type).getRawType());
        } else {
            // Handle other types if needed
            throw new IllegalArgumentException("Type is not a Class");
        }
    }

}
