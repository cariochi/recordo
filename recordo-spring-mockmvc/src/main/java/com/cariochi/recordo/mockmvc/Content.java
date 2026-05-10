package com.cariochi.recordo.mockmvc;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes request body content for lower-level MockMvc request annotations.
 * <p>
 * Set either {@link #value()} for inline content or {@link #file()} for content loaded from a resource file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Content {

    /**
     * Inline request body content.
     */
    String value() default "";

    /**
     * Resource file containing the request body.
     */
    String file() default "";

}
