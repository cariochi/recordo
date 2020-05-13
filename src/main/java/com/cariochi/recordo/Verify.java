package com.cariochi.recordo;

import java.lang.annotation.*;

@Repeatable(Verifies.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Verify {

    String value();

    boolean extensible() default true;

    boolean strictOrder() default false;

    String[] included() default {};

    String[] excluded() default {};

    String file() default "";

}
