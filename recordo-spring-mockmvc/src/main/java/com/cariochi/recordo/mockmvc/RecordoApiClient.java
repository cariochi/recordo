package com.cariochi.recordo.mockmvc;

import java.lang.annotation.*;

/**
 * Marks an interface as a typed MockMvc API client.
 * <p>
 * Recordo creates a runtime implementation whose methods are mapped with Spring MVC annotations such as
 * {@code @GetMapping}, {@code @PostMapping}, {@code @RequestBody}, and {@code @PathVariable}. Methods may
 * return a direct body, {@link Response}, or {@link Request}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RecordoApiClient {

    /**
     * Request interceptors applied to every request made by this client.
     */
    Class<? extends RequestInterceptor>[] interceptors() default {};

    /**
     * Name of an {@code ObjectMapper} or {@code JsonMapper} Spring bean or {@code @RecordoBean} field.
     */
    String objectMapper() default "";
}
