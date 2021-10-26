package com.cariochi.recordo.core;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
public @interface RecordoModule {

    Class<? extends Extension>[] extensions() default {};

    Class<? extends ParameterResolver>[] parameterResolvers() default {};

}
