package com.cariochi.recordo.mockmvc;

import java.util.function.Function;

@FunctionalInterface
public interface RequestInterceptor<T> extends Function<Request<T>, Request<T>> {

}
