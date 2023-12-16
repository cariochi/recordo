package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import java.util.Collection;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.web.servlet.MockMvc;

public class RecordoMockMvcCreator implements ObjectCreator {

    @Override
    public boolean isSupported(Class<?> targetClass) {
        return RecordoMockMvc.class.equals(targetClass);
    }

    @Override
    public <T> T create(Class<T> targetClass, ExtensionContext context) {
        if (!isSupported(targetClass)) {
            throw new IllegalArgumentException(targetClass.getName() + " not supported");
        }
        return (T) create("", context);
    }

    public RecordoMockMvc create(String objectMapper, ExtensionContext context) {
        final JsonConverter jsonConverter = JsonConverters.getJsonConverter(objectMapper, context);
        final Collection<RequestInterceptor> requestInterceptors = Beans.of(context).findAll(RequestInterceptor.class).values();
        final MockMvc mockMvc = Beans.of(context).findByType(MockMvc.class)
                .map(MockMvc.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Can't find single instance of MockMvc"));
        return new RecordoMockMvc(mockMvc, jsonConverter, requestInterceptors);
    }

}
