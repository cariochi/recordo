package com.cariochi.recordo.mockmvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.OK;

/**
 * This annotation is used to specify the details of a performing a request.
 * The details include the request path, method, headers, body, expected status, interceptors, and object mapper.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Perform {

    String path();

    HttpMethod method();

    String[] headers() default {};

    Content body() default @Content();

    HttpStatus expectedStatus() default OK;

    Class<? extends RequestInterceptor>[] interceptors() default {};

    String objectMapper() default "";
}
