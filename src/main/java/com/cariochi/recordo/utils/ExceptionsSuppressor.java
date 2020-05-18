package com.cariochi.recordo.utils;

import java.util.Objects;
import java.util.stream.Stream;

public final class ExceptionsSuppressor {

    private Class<? extends Throwable> exceptionType;

    public static ExceptionsSuppressor of(Class<? extends Throwable> exceptionType) {
        return new ExceptionsSuppressor(exceptionType);
    }

    private ExceptionsSuppressor(Class<? extends Throwable> exceptionType) {
        this.exceptionType = exceptionType;
    }

    public void executeAll(Stream<Runnable> commands) {
        commands
                .map(this::run)
                .filter(Objects::nonNull)
                .reduce((e1, e2) -> {
                    e1.addSuppressed(e2);
                    return e1;
                })
                .ifPresent(e -> {
                    if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                        throw (RuntimeException) e;
                    } else if (Error.class.isAssignableFrom(e.getClass())) {
                        throw (Error) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Throwable run(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable e) {
            if (exceptionType.isAssignableFrom(e.getClass())) {
                return e;
            } else {
                throw e;
            }
        }
    }

}
