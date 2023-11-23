package com.cariochi.recordo.read;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Loads a resource from json file.
 *
 * <ul>
 *     <li>If the file is absent, a new file with empty object will be created.</li>
 * </ul>
 *
 *<pre class="code"><code class="java">
 *
 *  &#064;Test
 *  void should_create_book(&#064;Read("/books/book.json") Book book) {
 *      ...
 *  }
 *
 *</code></pre>
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Read {

    String objectMapper() default "";

    String value();

}
