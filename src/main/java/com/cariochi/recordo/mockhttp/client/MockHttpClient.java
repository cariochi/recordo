package com.cariochi.recordo.mockhttp.client;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.mockhttp.client.dto.PageBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class MockHttpClient {

    private final JsonConverter jsonConverter;
    private final MockMvc mockMvc;

    public MockHttpClient(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.jsonConverter = new JacksonConverter(objectMapper);
    }

    public <RESP> Request<RESP> request(HttpMethod method, String path, Type responseType) {
        return new Request<>(this, method, path, responseType);
    }

    public <RESP> Request<RESP> request(HttpMethod method, String path, Class<RESP> responseType) {
        return request(method, path, (Type) responseType);
    }

    public <RESP> Request<RESP> request(HttpMethod method, String path, ParameterizedTypeReference<RESP> responseType) {
        return request(method, path, responseType.getType());
    }

    public <RESP> Request<RESP> get(String path, Class<RESP> responseType) {
        return request(HttpMethod.GET, path, responseType);
    }

    public <RESP> Request<RESP> get(String path, ParameterizedTypeReference<RESP> responseType) {
        return request(HttpMethod.GET, path, responseType);
    }

    public <RESP> Request<RESP> post(String path, Class<RESP> responseType) {
        return request(HttpMethod.POST, path, responseType);
    }

    public <RESP> Request<RESP> post(String path, ParameterizedTypeReference<RESP> responseType) {
        return request(HttpMethod.POST, path, responseType);
    }

    public <RESP> Request<RESP> put(String path, Class<RESP> responseType) {
        return request(HttpMethod.PUT, path, responseType);
    }

    public <RESP> Request<RESP> put(String path, ParameterizedTypeReference<RESP> responseType) {
        return request(HttpMethod.PUT, path, responseType);
    }

    public <RESP> Request<RESP> patch(String path, Class<RESP> responseType) {
        return request(HttpMethod.PATCH, path, responseType);
    }

    public <RESP> Request<RESP> patch(String path, ParameterizedTypeReference<RESP> responseType) {
        return request(HttpMethod.PATCH, path, responseType);
    }

    public <RESP> Request<RESP> delete(String path, Class<RESP> responseType) {
        return request(HttpMethod.DELETE, path, responseType);
    }

    public <RESP> Request<RESP> delete(String path, ParameterizedTypeReference<RESP> responseType) {
        return request(HttpMethod.DELETE, path, responseType);
    }

    public Request<Void> delete(String path) {
        return request(HttpMethod.DELETE, path, Void.class);
    }

    public <RESP> Response<RESP> execute(Request<RESP> request) {
        try {
            final MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.request(request.method(), request.path(), request.getUriVars())
                            .params(request.params());

            if (request.body() != null) {
                requestBuilder.contentType(MediaType.APPLICATION_JSON);
                requestBuilder.content(jsonConverter.toJson(request.body()));
            }

            request.headers().forEach(requestBuilder::header);

            final MockHttpServletResponse response = mockMvc
                    .perform(requestBuilder)
                    .andReturn()
                    .getResponse();

            Optional.ofNullable(request.expectedStatus())
                    .ifPresent(expected -> assertThat(response.getStatus()).isEqualTo(expected.value()));

            final String contentAsString = response.getContentAsString();

            return Response.<RESP>builder()
                    .status(HttpStatus.valueOf(response.getStatus()))
                    .headers(headersOf(response))
                    .body(isBlank(contentAsString) ? null : fromJson(contentAsString, request.responseType()))
                    .build();

        } catch (Exception e) {
            throw new RecordoError(e);
        }
    }

    private Map<String, String> headersOf(MockHttpServletResponse response) {
        return response.getHeaderNames().stream()
                .collect(toMap(
                        Function.identity(),
                        h -> response.getHeaderValues(h).stream().map(String::valueOf).collect(joining(", "))
                ));
    }

    private <RESP> RESP fromJson(String json, Type responseType) {
        if (responseType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) responseType;
            if (parameterizedType.getRawType().equals(Page.class)) {
                return pageFromJson(json, parameterizedType);
            }
        }
        return jsonConverter.fromJson(json, responseType);
    }

    private <RESP> RESP pageFromJson(String json, ParameterizedType parameterizedType) {
        final TypeFactory typeFactory = TypeFactory.defaultInstance();
        final JavaType pageItemType = typeFactory.constructType(parameterizedType.getActualTypeArguments()[0]);
        final JavaType pageType = typeFactory.constructParametricType(PageBuilder.class, pageItemType);
        final PageBuilder<?> pageBuilder = jsonConverter.fromJson(json, pageType);
        return (RESP) pageBuilder.build();
    }
}
