package com.cariochi.recordo.mockserver.interceptors.okhttp;

import com.cariochi.recordo.core.RecordoError;
import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static okio.Okio.buffer;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OkHttpMapper {

    public static final String CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8";

    public MockRequest toRecordoRequest(okhttp3.Request request) {
        return MockRequest.builder()
                .method(request.method())
                .url(request.url().url().toString())
                .headers(headersOf(request.headers()))
                .body(bodyOf(request))
                .build();
    }

    public MockResponse toRecordoResponse(Response response) {
        return MockResponse.builder()
                .protocol(response.protocol().toString())
                .headers(headersOf(response.headers()))
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

    private String bodyOf(okhttp3.Request request) {
        try {
            String requestContent = null;
            if (request.body() != null) {
                final Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                requestContent = buffer.readByteString().string(UTF_8);
            }
            return isEmpty(requestContent) ? null : requestContent;
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }

    private String bodyOf(Response response) {
        try {
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
        } catch (IOException e) {
            throw new RecordoError(e);
        }
    }

    private Map<String, String> headersOf(Headers headers) {
        return headers.toMultimap().entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> join(", ", e.getValue())));
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
