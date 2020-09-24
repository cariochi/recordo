package com.cariochi.recordo.mockhttp.client;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MockHttpDelete {

    String value();

    String[] headers() default {};

    HttpStatus expectedStatus() default OK;

    Class<? extends RequestInterceptor>[] interceptors() default {};
}
