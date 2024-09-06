package com.cariochi.recordo.mockserver.interceptors.resttemplate;

import com.cariochi.recordo.mockserver.model.MockRequest;
import com.cariochi.recordo.mockserver.model.MockResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

public class RestTemplateMapper {

    public MockRequest toRecordoRequest(HttpRequest request, byte[] body) {
        return MockRequest.builder()
                .method(Optional.ofNullable(request.getMethod()).map(HttpMethod::name).orElse(null))
                .url(request.getURI().toString())
                .headers(headersOf(request.getHeaders()))
                .body(Optional.ofNullable(body).filter(b -> b.length != 0).map(String::new).orElse(null))
                .build();
    }

    @SneakyThrows
    public MockResponse toRecordoResponse(ClientHttpResponse response) {
        return MockResponse.builder()
                .statusCode(response.getStatusCode().value())
                .statusText(response.getStatusText())
                .headers(headersOf(response.getHeaders()))
                .body(bodyOf(response))
                .build();
    }

    public ClientHttpResponse toHttpResponse(MockResponse recordoResponse) {
        return MockRestTemplateResponse.builder()
                .statusCode(HttpStatus.valueOf(recordoResponse.getStatusCode()))
                .headers(headersOf(recordoResponse.getHeaders()))
                .body(bodyOf(recordoResponse))
                .build();
    }

    @SneakyThrows
    private String bodyOf(ClientHttpResponse response) {
        final byte[] bytes = response.getBody().readAllBytes();
        return bytes.length == 0 ? null : new String(bytes);
    }

    private byte[] bodyOf(MockResponse recordoResponse) {
        return Optional.ofNullable(recordoResponse.getBody())
                .map(String.class::cast)
                .map(s -> s.getBytes(UTF_8))
                .orElse(new byte[0]);
    }

    private Map<String, String> headersOf(HttpHeaders headers) {
        return headers.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> join(", ", e.getValue())));
    }

    private HttpHeaders headersOf(Map<String, String> headers) {
        return headers.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> Stream.of(e.getValue().split(",")).toList(),
                        (u, v) -> u,
                        HttpHeaders::new
                ));
    }

    @Value
    @Builder
    public static class MockRestTemplateResponse implements ClientHttpResponse {

        HttpHeaders headers;
        byte[] body;
        HttpStatus statusCode;

        @Override
        public String getStatusText() {
            return statusCode.getReasonPhrase();
        }

        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @SneakyThrows
        @Override
        public void close() {
        }

    }

}
