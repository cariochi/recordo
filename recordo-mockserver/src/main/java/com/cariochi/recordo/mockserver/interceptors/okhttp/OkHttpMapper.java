package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static okio.Okio.buffer;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OkhttpMapper {

    public static final String CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8";

    public MockRequest toRecordoRequest(okhttp3.Request request) {
        final MediaType mediaType = Optional.ofNullable(request.body()).map(RequestBody::contentType).orElse(null);
        return MockRequest.builder()
                .method(request.method())
                .url(request.url().url().toString())
                .headers(headersOf(request.headers(), mediaType))
                .body(bodyOf(request))
                .build();
    }

    public MockResponse toRecordoResponse(Response response) {
        final MediaType mediaType = Optional.ofNullable(response.body()).map(ResponseBody::contentType).orElse(null);
        return MockResponse.builder()
                .protocol(response.protocol().toString())
                .headers(headersOf(response.headers(), mediaType))
                .statusCode(response.code())
                .statusText(response.message())
                .body(bodyOf(response))
                .build();
    }

    public Response toOkHttpResponse(okhttp3.Request request, MockResponse response) throws IOException {
        final byte[] body = bytes(response.getBody());
        final ResponseBody responseBody = ResponseBody.create(
                MediaType.parse(contentTypeOf(response.getHeaders())),
                body
        );
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.get(response.getProtocol().toLowerCase()))
                .code(response.getStatusCode())
                .message(Optional.ofNullable(response.getStatusText()).orElse(""))
                .headers(Headers.of(response.getHeaders()))
                .body(responseBody)
                .build();
    }

    @SneakyThrows
    private String bodyOf(okhttp3.Request request) {
        String requestContent = null;
        if (request.body() != null) {
            final Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            requestContent = buffer.readByteString().string(UTF_8);
        }
        return isEmpty(requestContent) ? null : requestContent;
    }

    @SneakyThrows
    private String bodyOf(Response response) {
        String responseContent = null;
        final ResponseBody body = response.body();
        if (body != null) {
            BufferedSource source = buffer(body.source());
            if ("gzip".equals(response.header("Content-Encoding"))) {
                source = buffer(new GzipSource(source));
            }
            responseContent = source.readUtf8();
        }
        return isEmpty(responseContent) ? null : responseContent;
    }

    private Map<String, String> headersOf(Headers headers, MediaType mediaType) {
        final LinkedHashMap<String, String> headerMap = headers.toMultimap().entrySet().stream()
                .collect(toMap(
                        Entry::getKey,
                        e -> join(", ", e.getValue()),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
        boolean hasContentType = headerMap.keySet().stream()
                .anyMatch(k -> k.equalsIgnoreCase("Content-Type"));
        if (!hasContentType && mediaType != null) {
            headerMap.put("Content-Type", mediaType.toString());
        }
        return headerMap;
    }

    private String contentTypeOf(Map<String, String> headers) {
        return Optional.ofNullable(headers.get(CONTENT_TYPE)).orElse(DEFAULT_CONTENT_TYPE);
    }

    private byte[] bytes(Object body) {
        return Optional.ofNullable(body)
                .map(String.class::cast)
                .map(s -> s.getBytes(UTF_8))
                .orElse(new byte[0]);
    }

}
