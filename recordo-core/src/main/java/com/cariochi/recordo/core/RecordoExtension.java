package com.cariochi.recordo.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader.Provider;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.toList;

@Slf4j
public class RecordoExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, ParameterResolver {

    @Getter
    private static ExtensionContext context;

    private final List<Extension> handlers = new ArrayList<>();

    @SneakyThrows
    public RecordoExtension() {

        load(RegularExtension.class).stream()
                .map(Provider::get)
                .forEach(handlers::add);

        if (isSpringContextAvailable()) {
            load(SpringExtension.class).stream()
                    .map(Provider::get)
                    .forEach(handlers::add);
        }

    }

    @SneakyThrows
    @Override
    public void beforeAll(ExtensionContext context) {
        RecordoExtension.context = context;
        final List<BeforeAllCallback> callbacks = handlers.stream()
                .filter(i -> BeforeAllCallback.class.isAssignableFrom(i.getClass()))
                .sorted(orderAnnotationComparator())
                .map(BeforeAllCallback.class::cast)
                .collect(toList());

        for (BeforeAllCallback callback : callbacks) {
            callback.beforeAll(context);
        }
    }

    @SneakyThrows
    @Override
    public void beforeEach(ExtensionContext context) {
        final List<BeforeEachCallback> callbacks = handlers.stream()
                .filter(i -> BeforeEachCallback.class.isAssignableFrom(i.getClass()))
                .sorted(orderAnnotationComparator())
                .map(BeforeEachCallback.class::cast)
                .collect(toList());

        for (BeforeEachCallback callback : callbacks) {
            callback.beforeEach(context);
        }
    }

    @SneakyThrows
    @Override
    public void afterEach(ExtensionContext context) {
        final List<AfterEachCallback> callbacks = handlers.stream()
                .filter(i -> AfterEachCallback.class.isAssignableFrom(i.getClass()))
                .sorted(orderAnnotationComparator())
                .map(AfterEachCallback.class::cast)
                .collect(toList());

        for (AfterEachCallback callback : callbacks) {
            callback.afterEach(context);
        }

    }

    @SneakyThrows
    @Override
    public void afterAll(ExtensionContext context) {
        final List<AfterAllCallback> callbacks = handlers.stream()
                .filter(i -> AfterAllCallback.class.isAssignableFrom(i.getClass()))
                .sorted(orderAnnotationComparator())
                .map(AfterAllCallback.class::cast)
                .collect(toList());

        for (AfterAllCallback callback : callbacks) {
            callback.afterAll(context);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return handlers.stream()
                .filter(i -> ParameterResolver.class.isAssignableFrom(i.getClass()))
                .map(ParameterResolver.class::cast)
                .anyMatch(r -> r.supportsParameter(parameter, extension));
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return handlers.stream()
                .filter(i -> ParameterResolver.class.isAssignableFrom(i.getClass()))
                .map(ParameterResolver.class::cast)
                .filter(r -> r.supportsParameter(parameter, extension))
                .findFirst()
                .map(r -> r.resolveParameter(parameter, extension))
                .orElse(null);
    }

    private boolean isSpringContextAvailable() {
        try {
            Class.forName("org.springframework.context.ApplicationContext");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Comparator<? super Extension> orderAnnotationComparator() {
        return (e1, e2) -> {
            final int v1 = Optional.of(e1.getClass()).map(c -> c.getAnnotation(Order.class)).map(Order::value).orElse(Integer.MAX_VALUE / 2);
            final int v2 = Optional.of(e2.getClass()).map(c -> c.getAnnotation(Order.class)).map(Order::value).orElse(Integer.MAX_VALUE / 2);
            return v1 - v2;
        };
    }

}
