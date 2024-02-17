package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.reflecto.types.ReflectoType;
import java.util.Collection;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.web.servlet.MockMvc;

public class RecordoMockMvcCreator implements ObjectCreator {

    @Override
    public boolean isSupported(ReflectoType type) {
        return type.is(RecordoMockMvc.class);
    }

    @Override
    public <T> T create(ReflectoType type, ExtensionContext context) {
        if (!isSupported(type)) {
            throw new IllegalArgumentException(type.name() + " not supported");
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
