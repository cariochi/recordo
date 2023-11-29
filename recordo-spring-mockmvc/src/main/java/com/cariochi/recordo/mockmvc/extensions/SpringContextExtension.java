package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoClient;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.createRecordoMockMvc;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

public class SpringContextExtension implements Extension, BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {

        final boolean needRecordoMockMvc = Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .anyMatch(SpringContextExtension::isRecordoMockMvcField);

        final List<Field> recordoClientFields = Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(SpringContextExtension::isRecordoClientField)
                .collect(toList());

        if (!needRecordoMockMvc && recordoClientFields.isEmpty()) {
            return;
        }

        if (Beans.of(context).findAll(RecordoMockMvc.class).isEmpty()) {
            registerRecordoMockMvc(context);
        }

        recordoClientFields.forEach(field -> registerRecordoClient(field.getType(), context));
    }

    private void registerRecordoMockMvc(ExtensionContext context) {
        final JsonConverter jsonConverter = JsonConverters.getJsonConverter("", context);
        final RecordoMockMvc recordoMockMvc = createRecordoMockMvc(context, jsonConverter);
        registerBean("recordoMockMvc", recordoMockMvc, context);
    }

    private <T> void registerRecordoClient(Class<T> targetClass, ExtensionContext context) {
        final RecordoClient annotation = targetClass.getAnnotation(RecordoClient.class);

        final List<RequestInterceptor> requestInterceptors = Stream.of(annotation.interceptors())
                .map(interceptorClass -> createRequestInterceptor(interceptorClass, context))
                .collect(toList());

        final JsonConverter jsonConverter = JsonConverters.getJsonConverter("", context);
        final RecordoMockMvc recordoMockMvc = createRecordoMockMvc(context, jsonConverter, emptySet());
        final RecordoClientProxyFactory proxyFactory = new RecordoClientProxyFactory(recordoMockMvc, requestInterceptors);
        final T recordoClient = proxyFactory.getRecordoClient(targetClass);
        registerBean(targetClass.getName(), recordoClient, context);
    }

    private RequestInterceptor createRequestInterceptor(Class<? extends RequestInterceptor> interceptorClass, ExtensionContext context) {
        return Beans.of(context).findByType(interceptorClass)
                .map(RequestInterceptor.class::cast)
                .orElseGet(() -> createRequestInterceptor(interceptorClass));
    }

    @SneakyThrows
    private RequestInterceptor createRequestInterceptor(Class<? extends RequestInterceptor> interceptorClass) {
        return interceptorClass.getConstructor().newInstance();
    }

    public <T> void registerBean(String beanName, T beanInstance, ExtensionContext context) {
        final BeanDefinition beanDefinition = genericBeanDefinition((Class<T>) beanInstance.getClass(), () -> beanInstance).getBeanDefinition();
        final GenericApplicationContext applicationContext = (GenericApplicationContext) getApplicationContext(context);
        applicationContext.registerBeanDefinition(beanName, beanDefinition);
    }

    private static boolean isRecordoMockMvcField(Field field) {
        return field.getType().isAssignableFrom(RecordoMockMvc.class) && field.isAnnotationPresent(Autowired.class);
    }

    private static boolean isRecordoClientField(Field field) {
        return field.getType().isAnnotationPresent(RecordoClient.class) && field.isAnnotationPresent(Autowired.class);
    }

}
