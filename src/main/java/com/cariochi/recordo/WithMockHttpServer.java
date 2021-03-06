package com.cariochi.recordo;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WithMockHttpServer {

    String value();

}
