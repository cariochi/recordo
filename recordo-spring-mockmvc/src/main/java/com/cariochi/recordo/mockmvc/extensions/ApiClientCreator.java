package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.ObjectCreator;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoApiClient;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;

import static java.util.stream.Collectors.toList;

public class ApiClientCreator implements ObjectCreator {

    private final RecordoMockMvcCreator recordoMockMvcCreator = new RecordoMockMvcCreator();

    @Override
    public boolean isSupported(Class<?> targetClass) {
        return targetClass.isAnnotationPresent(RecordoApiClient.class);
    }

    @Override
    public <T> T create(Class<T> targetClass, ExtensionContext context) {
        final RecordoApiClient annotation = targetClass.getAnnotation(RecordoApiClient.class);

        final RecordoMockMvc recordoMockMvc = recordoMockMvcCreator.create(annotation.objectMapper(), context);

        final List<RequestInterceptor> requestInterceptors = Stream.of(annotation.interceptors())
                .flatMap(interceptorClass -> createRequestInterceptor(interceptorClass, context).stream())
                .collect(toList());

        return ProxyFactory.of(targetClass)
                .newInstance(() -> new RecordoApiClientInvocationHandler(recordoMockMvc, requestInterceptors));
    }

    private <T extends RequestInterceptor> Collection<T> createRequestInterceptor(Class<T> interceptorClass, ExtensionContext context) {
        final Collection<T> beans = Beans.of(context).findAll(interceptorClass).values();
        return beans.isEmpty()
                ? Set.of(createRequestInterceptor(interceptorClass))
                : beans;
    }

    @SneakyThrows
    private <T extends RequestInterceptor> T createRequestInterceptor(Class<T> interceptorClass) {
        return interceptorClass.getConstructor().newInstance();
    }

}
