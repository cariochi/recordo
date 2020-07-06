package com.cariochi.recordo.verify;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
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
