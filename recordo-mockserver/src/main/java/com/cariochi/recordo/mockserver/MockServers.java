package com.cariochi.recordo.mockserver;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MockServers {

    MockServer[] value();

}
