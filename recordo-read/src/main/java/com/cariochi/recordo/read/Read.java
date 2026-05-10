package com.cariochi.recordo.read;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Loads a test fixture from a resource file.
 * <p>
 * {@code @Read} can be used on test parameters, fields, and methods in {@code @RecordoObjectFactory}
 * interfaces. If the target file does not exist, Recordo creates a fixture for the requested type and fails
 * the first run so the generated file can be reviewed.
 *
 * <pre class="code">
 * &#064;Test
 * void should_create_book(&#064;Read("/books/book.json") Book book) {
 *     ...
 * }
 * </pre>
 */
@Target({PARAMETER, FIELD, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Read {

    /**
     * Name of an {@code ObjectMapper} or {@code JsonMapper} Spring bean or {@code @RecordoBean} field.
     * Leave empty to use Recordo's default mapper or the single available mapper.
     */
    String objectMapper() default "";

    /**
     * Path to the fixture file under the configured Recordo resource root.
     */
    String value();

}
