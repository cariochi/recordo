package com.cariochi.recordo.read;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The Read annotation is used to specify that a resource should be loaded from a JSON file.
 * This annotation can be applied to a method parameter or class field.
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre class="code">
 * &#064;Test
 * void should_create_book(&#064;Read("/books/book.json") Book book) {
 *     ...
 * }
 * </pre>
 *
 * <p>
 * If the specified file path does not exist, a new file with a random object will be created.
 * </p>
 */
@Target({PARAMETER, FIELD, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Read {

    String objectMapper() default "";

    String value();

}
