package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;

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
        final Beans beans = Beans.of(context);
        final MockMvc mockMvc = beans.findByType(MockMvc.class).map(Beans.Bean::instance)
                .orElseThrow(() -> new IllegalArgumentException("Can't find single instance of MockMvc"));
        final Collection<RequestInterceptor> requestInterceptors = beans.findAll(RequestInterceptor.class).stream().map(Beans.Bean::instance).toList();
        return new RecordoMockMvc(mockMvc, jsonConverter, requestInterceptors);
    }

}
