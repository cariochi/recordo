package com.cariochi.recordo.mockmvc;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

import static org.springframework.http.HttpStatus.OK;

/**
 * Lower-level parameter annotation for preparing or performing an arbitrary MockMvc request.
 * <p>
 * Prefer {@link RecordoApiClient} for new tests.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Perform {

    /**
     * Request path.
     */
    String path();

    /**
     * HTTP method.
     */
    HttpMethod method();

    /**
     * Request headers in {@code Name: Value} form.
     */
    String[] headers() default {};

    /**
     * Request body content.
     */
    Content body() default @Content();

    /**
     * Expected response status. Recordo asserts this status when the request is performed.
     */
    HttpStatus expectedStatus() default OK;

    /**
     * Request interceptors applied to this request.
     */
    Class<? extends RequestInterceptor>[] interceptors() default {};

    /**
     * Name of an {@code ObjectMapper} or {@code JsonMapper} Spring bean or {@code @RecordoBean} field.
     */
    String objectMapper() default "";
}
