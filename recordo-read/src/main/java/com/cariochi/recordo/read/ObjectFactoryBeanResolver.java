package com.cariochi.recordo.read;

import com.cariochi.recordo.core.SpringContextExtension;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;


public class ObjectFactoryBeanResolver implements SpringContextExtension, BeforeAllCallback {

    private final ObjectFactoryCreator objectFactoryCreator = new ObjectFactoryCreator();

    @Override
    public void beforeAll(ExtensionContext context) {
        Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(field -> objectFactoryCreator.isSupported(field.getType()) && field.isAnnotationPresent(Autowired.class))
                .map(Field::getType)
                .forEach(type -> registerRecordoClient(type, context));
    }

    private <T> void registerRecordoClient(Class<T> targetClass, ExtensionContext context) {
        final T proxyInstance = objectFactoryCreator.create(targetClass, context);
        registerBean(targetClass.getName(), proxyInstance, context);
    }

}
