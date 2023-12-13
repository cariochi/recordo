package com.cariochi.recordo.read;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;


public class ObjectFactoryBeanResolver implements SpringContextExtension, BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(field -> field.getType().isAnnotationPresent(RecordoObjectFactory.class) && field.isAnnotationPresent(Autowired.class))
                .map(Field::getType)
                .forEach(type -> registerRecordoClient(type, context));
    }

    private <T> void registerRecordoClient(Class<T> targetClass, ExtensionContext context) {
        final ProxyFactory<T> proxyFactory = ProxyFactory.of(targetClass);
        final T proxyInstance = proxyFactory.newInstance(() -> new RecordoObjectFactoryInvocationHandler<>(proxyFactory, context));
        registerBean(targetClass.getName(), proxyInstance, context);
    }

}
