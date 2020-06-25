package com.cariochi.recordo.annotation;

import java.lang.annotation.*;

@Repeatable(Givens.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Given {

    String value();

    String field() default "";

}
