package com.cariochi.recordo.mockmvc;

public class AuthInterceptor implements RequestInterceptor {

    @Override
    public Request<?> apply(Request<?> request) {
        return request.header("Authorization", "Bearer TOKEN");
    }

}
