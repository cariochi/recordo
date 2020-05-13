package com.cariochi.recordo.junit4;

import com.cariochi.recordo.interceptor.RecordoInterceptors;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RecordoRule implements MethodRule {

    private final RecordoInterceptors interceptors;

    public RecordoRule() {
        this("", new JacksonConverter());
    }

    public RecordoRule(String rootFolder, JsonConverter jsonConverter) {
        this.interceptors = new RecordoInterceptors(rootFolder, jsonConverter);
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object testInstance) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                interceptors.beforeTest(testInstance, method.getMethod());
                statement.evaluate();
                interceptors.afterTest(testInstance, method.getMethod());
            }
        };
    }

}
