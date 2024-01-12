package com.cariochi.recordo.core;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface ObjectCreator {

    boolean isSupported(Class<?> targetClass);

    <T> T create(Class<T> targetClass, ExtensionContext context);

}
