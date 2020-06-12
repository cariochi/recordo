package com.cariochi.recordo.annotation;

import java.lang.annotation.*;

@Repeatable(Verifies.class)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Verify {

    String value() default "";

    String file() default "";

    boolean extensible() default false;

    boolean strictOrder() default true;

    String[] included() default {};

    String[] excluded() default {};

}
