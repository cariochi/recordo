package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.SpringExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.RecordoApiClient;
import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.reflecto.fields.JavaField;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.createRecordoMockMvc;
import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;


public class ApiClientFieldResolver implements SpringExtension, BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).fields().includeEnclosing().withAnnotation(EnableRecordo.class).stream()
                .filter(field -> field.getType().isAnnotationPresent(RecordoApiClient.class))
                .forEach(field -> createRecordoClient(context, field));
    }

    private void createRecordoClient(ExtensionContext context, JavaField field) {
        final RecordoApiClient annotation = field.getType().getAnnotation(RecordoApiClient.class);
        final List<RequestInterceptor> requestInterceptors = Stream.of(annotation.interceptors())
                .map(interceptorClass -> createRequestInterceptor(interceptorClass, context))
                .collect(toList());
        final JsonConverter jsonConverter = JsonConverters.getJsonConverter(annotation.objectMapper(), context);
        final RecordoMockMvc recordoMockMvc = createRecordoMockMvc(context, jsonConverter, emptySet());

        final Object recordoClient = ProxyFactory.of(field.getType())
                .newInstance(() -> new RecordoApiClientInvocationHandler(recordoMockMvc, requestInterceptors));
        field.setValue(recordoClient);
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

}
