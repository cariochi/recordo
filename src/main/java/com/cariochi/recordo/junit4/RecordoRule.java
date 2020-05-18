package com.cariochi.recordo.junit4;

import com.cariochi.recordo.interceptor.CompositeInterceptor;
import com.cariochi.recordo.interceptor.GivenInterceptor;
import com.cariochi.recordo.interceptor.VerifyInterceptor;
import com.cariochi.recordo.json.GsonConverter;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.function.Supplier;

public class RecordoRule implements MethodRule {

    private final CompositeInterceptor interceptor;

    public static RecordoRule withObjectMapper(Supplier<ObjectMapper> objectMapper) {
        return new RecordoRule(new JacksonConverter(objectMapper));
    }

    public static RecordoRule withGson(Supplier<Gson> gson) {
        return new RecordoRule(new GsonConverter(gson));
    }

    public RecordoRule() {
        this(new JacksonConverter());
    }

    public RecordoRule(JsonConverter jsonConverter) {
        this.interceptor = new CompositeInterceptor(
                new VerifyInterceptor(jsonConverter),
                new GivenInterceptor(jsonConverter)
        );
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                interceptor.beforeTest(testInstance, method.getMethod());
                statement.evaluate();
                interceptor.afterTest(testInstance, method.getMethod());
            }
        };
    }

}
