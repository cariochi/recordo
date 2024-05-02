package com.cariochi.recordo.mockserver.interceptors.apache;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class ApacheMapper {

    @SneakyThrows
    public MockRequest toRecordoRequest(ClassicHttpRequest request) {
        final String body = bodyOf(request.getEntity());
        return MockRequest.builder()
                .method(request.getMethod())
                .url(request.getUri().toString())
                .headers(headersOf(request.getHeaders()))
                .body(body)
                .build();
    }

    public MockResponse toRecordoResponse(ClassicHttpResponse response) {
        return MockResponse.builder()
                .protocol(response.getVersion().toString())
                .statusCode(response.getCode())
                .statusText(response.getReasonPhrase())
                .headers(headersOf(response.getHeaders()))
                .body(bodyOf(response.getEntity()))
                .build();
    }

    public ClassicHttpResponse toHttpResponse(MockResponse response) {
        final String protocol = substringBefore(response.getProtocol(), "/");
        final String[] version = substringAfter(response.getProtocol(), "/").split("\\.");
        final ResponseWrapper newResponse = new ResponseWrapper(
                new ProtocolVersion(protocol, Integer.parseInt(version[0]), Integer.parseInt(version[1])),
                response.getStatusCode(),
                response.getStatusText()
        );
        newResponse.setHeaders(response.getHeaders().entrySet().stream()
                .map(e -> new BasicHeader(e.getKey(), e.getValue()))
                .toArray(BasicHeader[]::new)
        );
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes(response.getBody()));
        BasicHttpEntity entity = new BasicHttpEntity(inputStream, ContentType.parse(response.contentType()));
        newResponse.setEntity(entity);
        return newResponse;
    }

    private String bodyOf(HttpEntity entity) {
        return Optional.ofNullable(entity)
                .map(ApacheMapper::entityToString)
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    @SneakyThrows
    private static String entityToString(HttpEntity entity) {
        return EntityUtils.toString(entity);
    }

    public Map<String, String> headersOf(Header[] headers) {
        return Stream.of(headers)
                .collect(groupingBy(
                        Header::getName,
                        mapping(Header::getValue, joining(", "))
                ));
    }

    private byte[] bytes(Object body) {
        return Optional.ofNullable(body)
                .map(String.class::cast)
                .map(s -> s.getBytes(UTF_8))
                .orElse(new byte[0]);
    }

    public static class ResponseWrapper extends BasicClassicHttpResponse {

        public ResponseWrapper(ProtocolVersion ver, int code, String reason) {
            super(code, reason);
            setVersion(ver);
        }

    }
}
