package com.cariochi.recordo.mockmvc.annotations;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Put {

    String value();

}
