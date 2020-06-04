package com.cariochi.recordo.junit4;

import com.cariochi.recordo.handler.CompositeAnnotationHandler;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RecordoRule implements MethodRule {

    private final CompositeAnnotationHandler interceptor = new CompositeAnnotationHandler();

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
