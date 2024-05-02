package com.cariochi.recordo.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

import static com.cariochi.reflecto.Reflecto.reflect;
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
                .filter(extension -> reflect(extension).type().is(BeforeAllCallback.class))
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
                .filter(extension -> reflect(extension).type().is(BeforeEachCallback.class))
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
                .filter(extension -> reflect(extension).type().is(AfterEachCallback.class))
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
                .filter(extension -> reflect(extension).type().is(AfterAllCallback.class))
                .sorted(orderAnnotationComparator())
                .map(AfterAllCallback.class::cast)
                .collect(toList());

        for (AfterAllCallback callback : callbacks) {
            callback.afterAll(context);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) throws ParameterResolutionException {
        return handlers.stream()
                .filter(extension -> reflect(extension).type().is(ParameterResolver.class))
                .map(ParameterResolver.class::cast)
                .anyMatch(r -> r.supportsParameter(parameter, context));
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext context) throws ParameterResolutionException {
        return handlers.stream()
                .filter(extension -> reflect(extension).type().is(ParameterResolver.class))
                .map(ParameterResolver.class::cast)
                .filter(parameterResolver -> parameterResolver.supportsParameter(parameter, context))
                .findFirst()
                .map(parameterResolver -> parameterResolver.resolveParameter(parameter, context))
                .orElse(null);
    }

    public static boolean isSpringContextAvailable() {
        return isClassAvailable("org.springframework.context.ApplicationContext");
    }

    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Comparator<? super Extension> orderAnnotationComparator() {
        return (extensionA, extensionB) -> {
            final int v1 = reflect(extensionA).type().annotations().find(Order.class).map(Order::value).orElse(Integer.MAX_VALUE / 2);
            final int v2 = reflect(extensionB).type().annotations().find(Order.class).map(Order::value).orElse(Integer.MAX_VALUE / 2);
            return v1 - v2;
        };
    }

}
