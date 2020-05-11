package com.recordo;

import java.lang.annotation.*;

@Repeatable(Givens.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Given {

    String value();

    String file() default "";
}
