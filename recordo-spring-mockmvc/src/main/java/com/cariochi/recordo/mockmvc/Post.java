package com.cariochi.recordo.mockmvc;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

import static org.springframework.http.HttpStatus.OK;


/**
 * Lower-level parameter annotation that prepares or performs a POST request with MockMvc.
 * <p>
 * Prefer {@link RecordoApiClient} for new tests.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Post {

    /**
     * Request path.
     */
    String value();

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

    /**
     * Multipart files attached to this request.
     */
    File[] files() default {};

    /**
     * Multipart file definition.
     */
    @Target({ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface File {

        /**
         * Multipart form field name.
         */
        String name();

        /**
         * Original file name sent in multipart metadata.
         */
        String originalFilename() default "";

        /**
         * Multipart content type.
         */
        String contentType() default "";

        /**
         * File content.
         */
        Content content() default @Content();

    }

}
