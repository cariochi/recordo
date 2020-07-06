package com.cariochi.recordo.mockmvc;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Headers {

    String[] value();
}
