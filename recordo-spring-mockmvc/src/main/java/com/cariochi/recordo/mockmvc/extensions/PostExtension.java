package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.utils.Files;
import com.cariochi.recordo.mockmvc.Content;
import com.cariochi.recordo.mockmvc.Post;
import com.cariochi.recordo.mockmvc.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.util.Optional;
import java.util.stream.Stream;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.POST;

public class PostExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(Post.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {

        final Post annotation = parameterContext.findAnnotation(Post.class).get();

        final Request<Object> request = getMockMvcClient(extensionContext)
                .request(POST, annotation.value(), getResponseType(parameterContext))
                .headers(parseHeaders(annotation.headers()))
                .expectedStatus(annotation.expectedStatus())
                .body(getBody(annotation.body(), extensionContext));

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
