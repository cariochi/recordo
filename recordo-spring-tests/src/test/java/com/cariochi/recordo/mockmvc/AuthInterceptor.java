package com.cariochi.recordo.mockmvc;

public class AuthInterceptor implements RequestInterceptor {

    @Override
    public Request<?> apply(Request<?> request) {
        if (request.headers().get("Authorization") == null) {
            request = request.header("Authorization", "Bearer TOKEN");
        }
        return request;
    }

}
