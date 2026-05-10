package com.cariochi.recordo.mockmvc;

import java.util.function.Function;

/**
 * Interceptor for modifying a MockMvc {@link Request} before it is performed.
 */
@FunctionalInterface
public interface RequestInterceptor extends Function<Request<?>, Request<?>> {

}
