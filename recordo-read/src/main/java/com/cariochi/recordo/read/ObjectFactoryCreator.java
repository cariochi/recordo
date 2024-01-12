package com.cariochi.recordo.read;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.reflecto.proxy.ProxyFactory;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ObjectFactoryCreator implements ObjectCreator {

    @Override
    public boolean isSupported(Class<?> targetClass) {
        return targetClass.isAnnotationPresent(RecordoObjectFactory.class);
    }

    @Override
    public <T> T create(Class<T> targetClass, ExtensionContext context) {
        final RecordoObjectFactoryMethodHandler<T> handler = new RecordoObjectFactoryMethodHandler<>(targetClass, context);
        return ProxyFactory.createInstance(handler, targetClass);
    }

}
