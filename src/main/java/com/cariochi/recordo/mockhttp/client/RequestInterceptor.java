package com.cariochi.recordo.mockhttp.client;

public interface RequestInterceptor {

    <T> Request<T> intercept(Request<T> request, MockHttpClient http);

}
