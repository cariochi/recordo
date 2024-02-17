package com.cariochi.recordo.read;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.reflecto.proxy.ProxyFactory;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ObjectFactoryCreator implements ObjectCreator {

    @Override
    public boolean isSupported(ReflectoType type) {
        return type.annotations().contains(RecordoObjectFactory.class);
    }

    @Override
    public <T> T create(ReflectoType type, ExtensionContext context) {
        final Class<T> aClass = (Class<T>) type.actualClass();
        final RecordoObjectFactoryMethodHandler<T> handler = new RecordoObjectFactoryMethodHandler<>(aClass, context);
        return ProxyFactory.createInstance(handler, aClass);
    }

}
