package com.cariochi.recordo.mockmvc;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

@Builder
@With
@AllArgsConstructor
public class Request<RESP> {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Object body;
    private final JsonConverter jsonConverter;
    private final MockMvc mockMvc;
    private final Type responseType;

    public Response<RESP> execute(Object... params) {
        try {
            final MockHttpServletRequestBuilder requestBuilder = request(method, path, params);

            if (body != null) {
                requestBuilder.contentType(MediaType.APPLICATION_JSON);
                requestBuilder.content(jsonConverter.toJson(body));
            }

            headers.forEach(requestBuilder::header);

            final MockHttpServletResponse response = mockMvc
                    .perform(requestBuilder)
                    .andReturn()
                    .getResponse();

            final String contentAsString = response.getContentAsString();

            return Response.<RESP>builder()
                    .statusCode(response.getStatus())
                    .headers(headersOf(response))
                    .body(Optional.of(contentAsString)
                            .filter(StringUtils::isNotBlank)
                            .map(c -> jsonConverter.<RESP>fromJson(c, responseType))
                            .orElse(null)
                    )
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
}
