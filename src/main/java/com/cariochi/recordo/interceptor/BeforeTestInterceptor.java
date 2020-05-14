package com.cariochi.recordo.interceptor;

import java.lang.reflect.Method;

public interface BeforeTestInterceptor extends Interceptor{

    void beforeTest(Object testInstance, Method method);

}
