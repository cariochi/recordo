package com.cariochi.recordo.read;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RegularExtension;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;


public class ObjectFactoryFieldResolver implements RegularExtension, BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                .withAnnotation(EnableRecordo.class).stream()
                .filter(field -> field.getType().isAnnotationPresent(RecordoObjectFactory.class))
                .forEach(field -> field.setValue(createProxyInstance(field.getType(), context)));
    }

    private <T> T createProxyInstance(Class<T> targetClass, ExtensionContext context) {
        final ProxyFactory<T> proxyFactory = ProxyFactory.of(targetClass);
        return proxyFactory.newInstance(() -> new RecordoObjectFactoryInvocationHandler<>(proxyFactory, context));
    }

}
