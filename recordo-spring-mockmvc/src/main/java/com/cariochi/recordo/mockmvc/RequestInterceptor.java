package com.cariochi.recordo.mockmvc;

import java.util.function.Function;

@FunctionalInterface
public interface RequestInterceptor extends Function<Request<?>, Request<?>> {

}
