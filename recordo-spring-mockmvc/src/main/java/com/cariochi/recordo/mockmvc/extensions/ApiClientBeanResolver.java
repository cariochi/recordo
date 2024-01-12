package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringContextExtension;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.stream.Collectors.toList;

public class ApiClientBeanResolver implements SpringContextExtension, BeforeAllCallback {

    private final ApiClientCreator apiClientCreator = new ApiClientCreator();

    @Override
    public void beforeAll(ExtensionContext context) {

        final List<Field> recordoClientFields = Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(this::isRecordoClientField)
                .collect(toList());

        recordoClientFields.stream()
                .map(Field::getType)
                .filter(type -> isBeanAbsent(type, context))
                .forEach(type -> registerRecordoClient(type, context));
    }

    private boolean isRecordoClientField(Field field) {
        return apiClientCreator.isSupported(field.getType()) && field.isAnnotationPresent(Autowired.class);
    }

    private <T> void registerRecordoClient(Class<T> targetClass, ExtensionContext context) {
        final T recordoClient = apiClientCreator.create(targetClass, context);
        registerBean(targetClass.getName(), recordoClient, context);
    }

}
