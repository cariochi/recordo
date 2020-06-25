package com.cariochi.recordo.mockmvc.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Body {

    String value();

}
