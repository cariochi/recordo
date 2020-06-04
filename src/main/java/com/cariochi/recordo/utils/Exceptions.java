package com.cariochi.recordo.utils;

import com.cariochi.recordo.RecordoError;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Exceptions {

    private Exceptions() {
    }

    public static <T> Supplier<T> trying(SupplierEx<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RecordoError(e);
            }
        };
    }

    public static <T, R> Function<T, R> trying(FunctionEx<T, R> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RecordoError(e);
            }
        };
    }

    @FunctionalInterface
    public interface SupplierEx<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public static interface FunctionEx<T, R> {
        R apply(T t) throws Exception;
    }
}
