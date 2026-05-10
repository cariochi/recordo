package com.cariochi.recordo.read;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks an interface as a Recordo fixture factory.
 * <p>
 * Methods annotated with {@link Read} load fixture files. Methods annotated with Objecto {@code @Modify}
 * create immutable factory variants that apply modifications before returning a fixture.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
public @interface RecordoObjectFactory {

}
