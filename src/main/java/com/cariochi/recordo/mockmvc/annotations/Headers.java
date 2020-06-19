package com.cariochi.recordo.mockmvc.annotations;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Headers {

    String[] value();
}
