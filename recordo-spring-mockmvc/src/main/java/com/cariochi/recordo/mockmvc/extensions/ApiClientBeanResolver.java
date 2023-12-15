package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringContextExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoApiClient;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.createRecordoMockMvc;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

public class ApiClientBeanResolver implements SpringContextExtension, BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {

        final List<Field> recordoClientFields = Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(this::isRecordoClientField)
                .collect(toList());

        recordoClientFields.stream()
                .map(Field::getType)
                .filter(type -> isBeanAbsent(type, context))
                .forEach(type -> registerRecordoClient(type, context));
    }

    private boolean isRecordoClientField(Field field) {
        return field.getType().isAnnotationPresent(RecordoApiClient.class) && field.isAnnotationPresent(Autowired.class);
    }

    private <T> void registerRecordoClient(Class<T> targetClass, ExtensionContext context) {
        final RecordoApiClient annotation = targetClass.getAnnotation(RecordoApiClient.class);

        final List<RequestInterceptor> requestInterceptors = Stream.of(annotation.interceptors())
                .flatMap(interceptorClass -> createRequestInterceptor(interceptorClass, context).stream())
                .collect(toList());

        final JsonConverter jsonConverter = JsonConverters.getJsonConverter(annotation.objectMapper(), context);
        final RecordoMockMvc recordoMockMvc = createRecordoMockMvc(context, jsonConverter, emptySet());

        final T recordoClient = ProxyFactory.of(targetClass)
                .newInstance(() -> new RecordoApiClientInvocationHandler(recordoMockMvc, requestInterceptors));
        registerBean(targetClass.getName(), recordoClient, context);
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
