package com.cariochi.recordo.mockmvc.utils;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.recordo.mockmvc.Content;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Response;
import com.cariochi.reflecto.types.ReflectoType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import static com.cariochi.reflecto.Reflecto.reflect;
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

    public static ReflectoType getResponseType(ReflectoType type) {
        if (type.is(Request.class)) {
            return type.arguments().get(0);
        } else if (type.is(Response.class)) {
            return type.arguments().get(0);
        } else {
            return type;
        }
    }

    public static Object getResponse(Request<Object> request, ReflectoType type) {
        if (type.is(Request.class)) {
            return request;
        } else if (type.is(Response.class)) {
            return request.perform();
        } else {
            return request.perform().getBody();
        }
    }

    private static String getBodyFromFile(String bodyFile, JsonConverter jsonConverter) {
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        return (String) objectReader.read(bodyFile, reflect(String.class));
    }

}
