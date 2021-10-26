package com.cariochi.recordo.mockmvc;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Perform {

    String path();

    HttpMethod method();

    String[] headers() default {};

    String body() default "";

    HttpStatus expectedStatus() default OK;

    Class<? extends RequestInterceptor>[] interceptors() default {};
}
