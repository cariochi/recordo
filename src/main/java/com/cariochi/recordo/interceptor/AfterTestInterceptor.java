package com.cariochi.recordo.interceptor;

import java.lang.reflect.Method;

public interface AfterTestInterceptor extends Interceptor{

    void afterTest(Object testInstance, Method method);

}
