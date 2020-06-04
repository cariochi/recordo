package com.cariochi.recordo.handler;

import java.lang.reflect.Method;

public interface BeforeTestHandler extends AnnotationHandler {

    void beforeTest(Object testInstance, Method method);

}
