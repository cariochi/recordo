package com.cariochi.recordo.mockmvc;

public interface RequestInterceptor {

    <T> Request<T> intercept(Request<T> request, RecordoMockMvc http);

}
