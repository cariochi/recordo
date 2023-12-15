package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.createRecordoMockMvc;

public class RecordoMockMvcBeanResolver implements SpringContextExtension, BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {

        final boolean needRecordoMockMvc = Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .anyMatch(RecordoMockMvcBeanResolver::isRecordoMockMvcField);

        if (needRecordoMockMvc && isBeanAbsent(RecordoMockMvc.class, context)) {
            registerDefaultRecordoMockMvc(context);
        }

    }

    private static boolean isRecordoMockMvcField(Field field) {
        return field.getType().isAssignableFrom(RecordoMockMvc.class) && field.isAnnotationPresent(Autowired.class);
    }

    private void registerDefaultRecordoMockMvc(ExtensionContext context) {
        final JsonConverter jsonConverter = JsonConverters.getJsonConverter("", context);
        final RecordoMockMvc recordoMockMvc = createRecordoMockMvc(context, jsonConverter);
        registerBean("recordoMockMvc", recordoMockMvc, context);
    }

}
