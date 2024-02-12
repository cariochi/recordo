package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.reflecto.fields.ReflectoField;
import com.cariochi.reflecto.types.ReflectoType;
import java.util.List;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.stream.Collectors.toList;

public class ApiClientBeanResolver implements SpringContextExtension, BeforeAllCallback {

    private final ApiClientCreator apiClientCreator = new ApiClientCreator();

    @Override
    public void beforeAll(ExtensionContext context) {

        final List<ReflectoField> recordoClientFields = reflect(context.getRequiredTestClass()).includeEnclosing().fields().stream()
                .filter(this::isRecordoClientField)
                .collect(toList());

        recordoClientFields.stream()
                .map(ReflectoField::type)
                .filter(type -> isBeanAbsent(type.actualClass(), context))
                .forEach(type -> registerRecordoClient(type, context));
    }

    private boolean isRecordoClientField(ReflectoField field) {
        return apiClientCreator.isSupported(field.type()) && field.annotations().contains(Autowired.class);
    }

    private <T> void registerRecordoClient(ReflectoType type, ExtensionContext context) {
        final T recordoClient = apiClientCreator.create(type, context);
        registerBean(type.name(), recordoClient, context);
    }

}
