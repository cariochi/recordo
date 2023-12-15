package com.cariochi.recordo.mockmvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.OK;

/**
 * Annotation used to describe a PATCH request.
 * It can be applied to a method parameter to specify the request details.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Patch {

    String value();

    String[] headers() default {};

    Content body() default @Content();

    HttpStatus expectedStatus() default OK;

    Class<? extends RequestInterceptor>[] interceptors() default {};

    String objectMapper() default "";
}
