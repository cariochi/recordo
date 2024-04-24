package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.mockmvc.dto.PageBuilder;
import com.cariochi.recordo.mockmvc.dto.SliceBuilder;
import com.cariochi.reflecto.types.ReflectoType;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
public class RecordoMockMvc {

    private final MockMvc mockMvc;
    private final JsonConverter jsonConverter;
    private final Collection<RequestInterceptor> requestInterceptors;

    public RecordoMockMvc(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.jsonConverter = new JsonConverter(objectMapper);
        this.requestInterceptors = emptySet();
    }

    public RecordoMockMvc(MockMvc mockMvc, ObjectMapper objectMapper, Collection<RequestInterceptor> requestInterceptors) {
        this.mockMvc = mockMvc;
        this.jsonConverter = new JsonConverter(objectMapper);
        this.requestInterceptors = requestInterceptors;
    }

    // Request

    public <RESP> Request<RESP> request(HttpMethod method, String path, Type responseType) {
        return new Request<>(this, method, path, responseType);
    }

    public <RESP> Request<RESP> request(HttpMethod method, String path, Class<RESP> responseType) {
        return request(method, path, (Type) responseType);
    }

    // GET

    public <RESP> Request<RESP> get(String path, Type responseType) {
        return request(GET, path, responseType);
    }

    public <RESP> Request<RESP> get(String path, Class<RESP> responseType) {
        return request(GET, path, responseType);
    }

    // POST

    public <RESP> Request<RESP> post(String path, Type responseType) {
        return request(POST, path, responseType);
    }

    public <RESP> Request<RESP> post(String path, Class<RESP> responseType) {
        return request(POST, path, responseType);
    }

    // PUT

    public <RESP> Request<RESP> put(String path, Type responseType) {
        return request(PUT, path, responseType);
    }

    public <RESP> Request<RESP> put(String path, Class<RESP> responseType) {
        return request(PUT, path, responseType);
    }

    // PATCH

    public <RESP> Request<RESP> patch(String path, Type responseType) {
        return request(PATCH, path, responseType);
    }

    public <RESP> Request<RESP> patch(String path, Class<RESP> responseType) {
        return request(PATCH, path, responseType);
    }

    // DELETE

    public <RESP> Request<RESP> delete(String path, Type responseType) {
        return request(DELETE, path, responseType);
    }

    public <RESP> Request<RESP> delete(String path, Class<RESP> responseType) {
        return request(DELETE, path, responseType);
    }

    public Request<Void> delete(String path) {
        return request(DELETE, path, Void.class);
    }


    @SneakyThrows
    public <RESP> Response<RESP> perform(Request<RESP> request) {

        for (RequestInterceptor interceptor : requestInterceptors) {
            request = (Request<RESP>) interceptor.apply(request);
        }

        MockHttpServletRequestBuilder requestBuilder;

        if (request.files().isEmpty()) {
            requestBuilder = MockMvcRequestBuilders.request(request.method(), request.path(), request.uriVars());
        } else {
            MockMultipartHttpServletRequestBuilder multipart = MockMvcRequestBuilders.multipart(request.path(), request.uriVars());
            request.files().stream()
                    .map(file -> new MockMultipartFile(file.name(), file.originalFilename(), file.contentType(), file.content()))
                    .forEach(multipart::file);

            final HttpMethod method = request.method();
            if (!POST.equals(method)) {
                multipart.with(r -> {
                    r.setMethod(method.name());
                    return r;
                });
            }

            requestBuilder = multipart;
        }

        requestBuilder.params(request.params());

        final Object body = request.body();
        if (body != null) {
            String contentType = request.headers().getOrDefault(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            requestBuilder.contentType(contentType);
            if (body instanceof String) {
                requestBuilder.content((String) body);
            } else if (body instanceof byte[]) {
                requestBuilder.content((byte[]) body);
            } else if (contentType.startsWith(APPLICATION_JSON_VALUE)) {
                requestBuilder.content(jsonConverter.toJson(body));
            } else {
                throw new IllegalArgumentException(format("Content Type %s not supported", contentType));
            }
        }

        request.headers().forEach(requestBuilder::header);

        final MockHttpServletResponse response = mockMvc
                .perform(requestBuilder)
                .andReturn()
                .getResponse();

        Optional.ofNullable(request.expectedStatus())
                .map(HttpStatus::value)
                .ifPresent(expectedStatus -> assertThat(response.getStatus()).isEqualTo(expectedStatus));

        return Response.<RESP>builder()
                .status(HttpStatus.valueOf(response.getStatus()))
                .headers(headersOf(response))
                .body(getBody(request, response))
                .build();

    }

    @SneakyThrows
    private <RESP> RESP getBody(Request<RESP> request, MockHttpServletResponse response) {
        final ReflectoType responseType = reflect(request.responseType());
        if (response.getContentAsByteArray().length == 0) {
            return null;
        } else if (responseType.is(byte[].class)) {
            return (RESP) response.getContentAsByteArray();
        } else if (responseType.is(String.class)) {
            return (RESP) response.getContentAsString();
        } else if (responseType.isParametrized()) {
            final String contentAsString = response.getContentAsString();
            if (responseType.actualClass().equals(Page.class)) {
                return pageFromJson(contentAsString, responseType);
            } else if (responseType.actualClass().equals(Slice.class)) {
                return sliceFromJson(contentAsString, responseType);
            }
        }
        return jsonConverter.fromJson(response.getContentAsString(), responseType.actualType());
    }

    private Map<String, String> headersOf(MockHttpServletResponse response) {
        return response.getHeaderNames().stream()
                .collect(toMap(
                        identity(),
                        h -> response.getHeaderValues(h).stream().map(String::valueOf).collect(joining(", "))
                ));
    }

    private <RESP> RESP pageFromJson(String json, ReflectoType type) {
        final TypeFactory typeFactory = TypeFactory.defaultInstance();
        final JavaType pageItemType = typeFactory.constructType(type.arguments().get(0).actualType());
        final JavaType pageType = typeFactory.constructParametricType(PageBuilder.class, pageItemType);
        final PageBuilder<?> pageBuilder = jsonConverter.fromJson(json, pageType);
        return (RESP) pageBuilder.build();
    }

    private <RESP> RESP sliceFromJson(String json, ReflectoType type) {
        final TypeFactory typeFactory = TypeFactory.defaultInstance();
        final JavaType sliceItemType = typeFactory.constructType(type.arguments().get(0).actualType());
        final JavaType pageType = typeFactory.constructParametricType(SliceBuilder.class, sliceItemType);
        final SliceBuilder<?> sliceBuilder = jsonConverter.fromJson(json, pageType);
        return (RESP) sliceBuilder.build();
    }

}
