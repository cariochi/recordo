package com.cariochi.recordo.mockmvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.OK;

/**
 * Annotation to indicate that a method should perform a GET request.
 * This annotation can be added to a method parameter.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Get {

    String value();

    String[] headers() default {};

    HttpStatus expectedStatus() default OK;

    Class<? extends RequestInterceptor>[] interceptors() default {};

    String objectMapper() default "";

}
