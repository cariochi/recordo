package com.cariochi.recordo.annotation;

import java.lang.annotation.*;

@Repeatable(Verifies.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Verify {

    String value();

    String field() default "";

    boolean extensible() default false;

    boolean strictOrder() default true;

    String[] included() default {};

    String[] excluded() default {};

}
