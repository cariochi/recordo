package com.cariochi.recordo.junit5;

import com.cariochi.recordo.interceptor.CompositeInterceptor;
import com.cariochi.recordo.interceptor.GivenInterceptor;
import com.cariochi.recordo.interceptor.VerifyInterceptor;
import com.cariochi.recordo.json.GsonConverter;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.function.Supplier;

public class RecordoExtension implements BeforeEachCallback, AfterEachCallback {

    private final CompositeInterceptor interceptor;

    public static RecordoExtension withObjectMapper(Supplier<ObjectMapper> objectMapper) {
        return new RecordoExtension(new JacksonConverter(objectMapper));
    }

    public static RecordoExtension withGson(Supplier<Gson> gson) {
        return new RecordoExtension(new GsonConverter(gson));
    }

    public RecordoExtension() {
        this(new JacksonConverter());
    }

    private RecordoExtension(JsonConverter jsonConverter) {
        this.interceptor = new CompositeInterceptor(
                new VerifyInterceptor(jsonConverter),
                new GivenInterceptor(jsonConverter)
        );
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.beforeTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.afterTest(testInstance, context.getRequiredTestMethod())
        );
    }

}
