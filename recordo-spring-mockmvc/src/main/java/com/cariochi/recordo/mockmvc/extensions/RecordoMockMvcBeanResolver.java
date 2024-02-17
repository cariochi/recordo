package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.reflecto.fields.ReflectoField;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cariochi.reflecto.Reflecto.reflect;

public class RecordoMockMvcBeanResolver implements SpringContextExtension, BeforeAllCallback {

    private final RecordoMockMvcCreator recordoMockMvcCreator = new RecordoMockMvcCreator();

    @Override
    public void beforeAll(ExtensionContext context) {
        final boolean needRecordoMockMvc = reflect(context.getRequiredTestClass()).fields().stream()
                .anyMatch(this::isRecordoMockMvcField);

        if (needRecordoMockMvc && isBeanAbsent(RecordoMockMvc.class, context)) {
            registerDefaultRecordoMockMvc(context);
        }

    }

    private boolean isRecordoMockMvcField(ReflectoField field) {
        return field.type().isAssignableFrom(RecordoMockMvc.class) && field.annotations().contains(Autowired.class);
    }

    private void registerDefaultRecordoMockMvc(ExtensionContext context) {
        final RecordoMockMvc recordoMockMvc = recordoMockMvcCreator.create("", context);
        registerBean("recordoMockMvc", recordoMockMvc, context);
    }

}
