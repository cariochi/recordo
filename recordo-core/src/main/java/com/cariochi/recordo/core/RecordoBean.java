package com.cariochi.recordo.core;

import java.lang.annotation.*;

/**
 * Marks a test class field as a named Recordo-managed object.
 * <p>
 * Recordo can resolve such fields by field name when an annotation accepts a named dependency, for example
 * {@code objectMapper = "customMapper"} or {@code client = "githubClient"}. In Spring tests, Spring beans and
 * {@code @RecordoBean} fields participate in the same lookup rules.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RecordoBean {

}
