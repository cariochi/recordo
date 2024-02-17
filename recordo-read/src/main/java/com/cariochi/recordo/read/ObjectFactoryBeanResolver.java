package com.cariochi.recordo.read;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.reflecto.fields.ReflectoField;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cariochi.reflecto.Reflecto.reflect;


public class ObjectFactoryBeanResolver implements SpringContextExtension, BeforeAllCallback {

    private final ObjectFactoryCreator objectFactoryCreator = new ObjectFactoryCreator();

    @Override
    public void beforeAll(ExtensionContext context) {
        reflect(context.getRequiredTestClass()).fields().stream()
                .filter(field -> objectFactoryCreator.isSupported(field.type()) && field.annotations().contains(Autowired.class))
                .map(ReflectoField::type)
                .forEach(type -> registerRecordoClient(type, context));
    }

    private void registerRecordoClient(ReflectoType type, ExtensionContext context) {
        final Object proxyInstance = objectFactoryCreator.create(type, context);
        registerBean(type.name(), proxyInstance, context);
    }

}
