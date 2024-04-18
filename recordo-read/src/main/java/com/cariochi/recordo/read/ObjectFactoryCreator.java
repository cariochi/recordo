package com.cariochi.recordo.read;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.proxy;

public class ObjectFactoryCreator implements ObjectCreator {

    @Override
    public boolean isSupported(ReflectoType type) {
        return type.annotations().contains(RecordoObjectFactory.class);
    }

    @Override
    public <T> T create(ReflectoType type, ExtensionContext context) {
        return proxy(type.actualClass())
                .with(() -> new ObjectFactoryProxyHandler<>(type.actualClass(), context))
                .getConstructor()
                .newInstance();
    }

}
