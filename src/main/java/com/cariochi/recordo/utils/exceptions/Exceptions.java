package com.cariochi.recordo.utils.exceptions;

import com.cariochi.recordo.RecordoError;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class Exceptions {

    public ExceptionsCollector collectorOf(Class<? extends Throwable> exceptionType) {
        return ExceptionsCollector.of(exceptionType);
    }

    public static <T> Supplier<T> tryGet(SupplierEx<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RecordoError(e);
            }
        };
    }

    public static <T> Consumer<T> tryAccept(ConsumerEx<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                throw new RecordoError(e);
            }
        };
    }

    public static <T, R> Function<T, R> tryApply(FunctionEx<T, R> function) {
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
    public interface ConsumerEx<T> {
        void accept(T value) throws Exception;
    }

    @FunctionalInterface
    public interface FunctionEx<T, R> {
        R apply(T t) throws Exception;
    }
}
