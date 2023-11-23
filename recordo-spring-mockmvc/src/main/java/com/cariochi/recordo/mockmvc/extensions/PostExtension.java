package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.Files;
import com.cariochi.recordo.mockmvc.Content;
import com.cariochi.recordo.mockmvc.Post;
import com.cariochi.recordo.mockmvc.Request;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getBody;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getMockMvcClient;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.parseHeaders;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.POST;

public class PostExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Post.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        final Post annotation = parameterContext.findAnnotation(Post.class).orElseThrow();

        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), extensionContext);

        final Request<Object> request = getMockMvcClient(extensionContext, jsonConverter)
                .request(POST, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), jsonConverter));

        Stream.of(annotation.files())
                .map(file -> Request.File.builder()
                        .name(file.name())
                        .originalFilename(file.originalFilename())
                        .contentType(file.contentType())
                        .content(getFileContent(file.content()))
                        .build()
                )
                .forEach(request::file);

        return processRequest(request, annotation.interceptors(), parameterContext, extensionContext);
    }

    private static byte[] getFileContent(Content content) {
        return Optional.ofNullable(content.value())
                .filter(StringUtils::isNotBlank)
                .map(s -> s.getBytes(UTF_8))
                .or(() -> Optional.ofNullable(content.file())
                        .filter(StringUtils::isNotBlank)
                        .map(Files::readBytes)
                )
                .orElse(null);
    }

}
