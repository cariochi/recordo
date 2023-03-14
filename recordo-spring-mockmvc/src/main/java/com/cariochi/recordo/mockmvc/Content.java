package com.cariochi.recordo.mockmvc;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Content {

    String value() default "";

    String file() default "";

}
