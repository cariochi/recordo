package com.cariochi.recordo.handler;

import java.lang.reflect.Method;

public interface AfterTestHandler extends AnnotationHandler {

    void afterTest(Object testInstance, Method method);

}
