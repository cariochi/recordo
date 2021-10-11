package com.cariochi.recordo;

import java.lang.annotation.*;

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

    String value();

}
