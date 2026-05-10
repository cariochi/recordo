package com.cariochi.recordo.mockserver;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable {@link MockServer} declarations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MockServers {

    /**
     * Mock server declarations applied to the same test method.
     */
    MockServer[] value();

}
