package com.cariochi.recordo.mockhttp.interceptors.okhttp;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.mockhttp.model.MockHttpRequest;
import com.cariochi.recordo.mockhttp.model.MockHttpResponse;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OkHttpMapper {

    public static final String CONTENT_TYPE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8";

    public MockHttpRequest toRecordoRequest(okhttp3.Request request) {
        return  MockHttpRequest.builder()
                .method(request.method())
                .url(request.url().url().toString())
                .headers(headersOf(request.headers()))
                .body(bodyOf(request))
                .build();
    }

    public MockHttpResponse toRecordoResponse(okhttp3.Response response) {
        return  MockHttpResponse.builder()
                .protocol(response.protocol().toString())
                .headers(headersOf(response.headers()))
                .statusCode(response.code())
                .statusText(response.message())
                .body(bodyOf(response))
                .build();
    }

    public okhttp3.Response fromRecordoResponse(okhttp3.Request request, MockHttpResponse response) throws IOException {
        final byte[] body = bytes(response.getBody());
        final ResponseBody responseBody = ResponseBody.create(
                MediaType.parse(contentTypeOf(response.getHeaders())),
                body
        );
        return new okhttp3.Response.Builder()
                .request(request)
                .code(response.getStatusCode())
                .message(response.getStatusText())
                .protocol(Protocol.get(response.getProtocol().toLowerCase()))
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

    private String bodyOf(okhttp3.Response response) {
        try {
            String responseContent = null;
            if (response.body() != null) {
                responseContent = response.body().source().readString(UTF_8);
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
