package com.cariochi.recordo.junit4;

import com.cariochi.recordo.interceptor.GivenInterceptor;
import com.cariochi.recordo.interceptor.RecordoInterceptor;
import com.cariochi.recordo.interceptor.VerifyInterceptor;
import com.cariochi.recordo.json.JacksonConverter;
import com.cariochi.recordo.json.JsonConverter;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RecordoRule implements MethodRule {

    private final RecordoInterceptor interceptors;

    public RecordoRule() {
        this(new JacksonConverter());
    }

    public RecordoRule(JsonConverter jsonConverter) {
        final VerifyInterceptor verifyInterceptor = new VerifyInterceptor(jsonConverter);
        final GivenInterceptor givenInterceptor = new GivenInterceptor(jsonConverter);
        this.interceptors = RecordoInterceptor.builder()
                .interceptor(verifyInterceptor)
                .interceptor(givenInterceptor)
                .build();
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
