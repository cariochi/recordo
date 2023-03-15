package com.cariochi.recordo.mockmvc.utils;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.recordo.mockmvc.Content;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Response;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.core.utils.BeanUtils.findBean;
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

    public static String getBody(Content content, ExtensionContext context) {
        return Optional.ofNullable(content.value())
                .filter(StringUtils::isNotBlank)
                .or(() -> Optional.ofNullable(content.file())
                        .filter(StringUtils::isNotBlank)
                        .map(file -> getBodyFromFile(file, context))
                )
                .orElse(null);
    }

    public static RecordoMockMvc getMockMvcClient(ExtensionContext context) {
        final MockMvc mockMvc = findBean(MockMvc.class, context).map(MockMvc.class::cast).orElseThrow();
        final JsonConverter jsonConverter = getJsonConverter(context);
        return new RecordoMockMvc(mockMvc, jsonConverter);
    }

    public static Type getResponseType(ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        final Type parameterType = parameter.getParameter().getParameterizedType();
        if (isRequestType(parameterClass)) {
            return ((ParameterizedType) parameterType).getActualTypeArguments()[0];
        } else if (isResponseType(parameterClass)) {
            return ((ParameterizedType) parameterType).getActualTypeArguments()[0];
        } else {
            return parameterType;
        }
    }

    public static boolean isRequestType(Class<?> type) {
        return Request.class.isAssignableFrom(type);
    }

    public static boolean isResponseType(Class<?> type) {
        return Response.class.isAssignableFrom(type);
    }

    private static String getBodyFromFile(String bodyFile, ExtensionContext context) {
        final JsonConverter jsonConverter = getJsonConverter(context);
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        return (String) objectReader.read(bodyFile, String.class);
    }

}