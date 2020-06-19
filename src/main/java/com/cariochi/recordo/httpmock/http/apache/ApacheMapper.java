package com.cariochi.recordo.httpmock.http.apache;

import com.cariochi.recordo.httpmock.model.RequestMock;
import com.cariochi.recordo.httpmock.model.ResponseMock;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.*;

public class ApacheMapper {

    public RequestMock toRecordoRequest(HttpRequestWrapper wrapper) throws IOException {
        final HttpRequest request = wrapper.getOriginal();
        final String body = request instanceof HttpEntityEnclosingRequest
                ? bodyOf(((HttpEntityEnclosingRequest) request).getEntity())
                : null;
        return RequestMock.builder()
                .method(request.getRequestLine().getMethod())
                .url(request.getRequestLine().getUri())
                .headers(headersOf(request.getAllHeaders()))
                .body(body)
                .build();
    }

    public ResponseMock toRecordoResponse(HttpResponse response) throws IOException {
        return ResponseMock.builder()
                .protocol(response.getProtocolVersion().toString())
                .statusCode(response.getStatusLine().getStatusCode())
                .statusText(response.getStatusLine().getReasonPhrase())
                .headers(headersOf(response.getAllHeaders()))
                .body(bodyOf(response.getEntity()))
                .build();
    }

    public CloseableHttpResponse fromRecordoResponse(ResponseMock response) {
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
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new ByteArrayInputStream(bytes(response.getBody())));
        newResponse.setEntity(entity);
        return newResponse;
    }

    private String bodyOf(HttpEntity entity) throws IOException {
        String bytes = null;
        if (entity != null) {
            try (InputStream is = entity.getContent()) {
                bytes = IOUtils.toString(is, UTF_8);
            }
        }
        return isEmpty(bytes) ? null : bytes;
    }

    public Map<String, String> headersOf(Header[] headers) {
        return stream(headers)
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

    public static class ResponseWrapper extends BasicHttpResponse implements CloseableHttpResponse {

        public ResponseWrapper(ProtocolVersion ver, int code, String reason) {
            super(ver, code, reason);
        }

        @Override
        public void close() {

        }
    }
}
