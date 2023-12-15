package com.cariochi.recordo.mockmvc;

import java.util.function.Function;

/**
 * Represents a functional interface for intercepting requests.
 * It extends the Function interface and serves as an interceptor for a Request object.
 */
@FunctionalInterface
public interface RequestInterceptor extends Function<Request<?>, Request<?>> {

}
