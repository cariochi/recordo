package com.cariochi.recordo.annotation;

import java.lang.annotation.*;

@Repeatable(Verifies.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Verify {

    String value();

    boolean extensible() default false;

    boolean strictOrder() default true;

    String[] included() default {};

    String[] excluded() default {};

    String file() default "";

}
