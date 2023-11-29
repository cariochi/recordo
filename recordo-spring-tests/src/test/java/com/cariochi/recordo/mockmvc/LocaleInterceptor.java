package com.cariochi.recordo.mockmvc;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LocaleInterceptor implements RequestInterceptor {

    private final String locale;

    @Override
    public Request<?> apply(Request<?> request) {
        return request.header("Locale", locale);
    }

}
