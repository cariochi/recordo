package com.cariochi.recordo.read;

import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.proxy.ProxyFactory;
import com.cariochi.recordo.core.utils.ObjectReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtensionContext;

@RequiredArgsConstructor
public class RecordoObjectFactoryInvocationHandler<T> implements InvocationHandler {

    private final ProxyFactory<T> proxyFactory;
    private final ExtensionContext context;
    private final Map<String, Object> handlerParameters = new LinkedHashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        final Map<String, Object> methodParameters = readMethodParameters(method, args);

        if (method.getReturnType().equals(method.getDeclaringClass())) {
            return createChildProxyInstance(methodParameters);
        } else {

            final Read read = method.getAnnotation(Read.class);
            if (read == null) {
                throw new IllegalArgumentException("@Read annotation required");
            }

            final JsonConverter jsonConverter = JsonConverters.getJsonConverter(read.objectMapper(), context);
            final ObjectFactory<T> objectFactory = new ObjectFactory<>(new ObjectReader(jsonConverter), read.value(), method.getGenericReturnType());

            final Map<String, Object> tmp = new LinkedHashMap<>();
            tmp.putAll(handlerParameters);
            tmp.putAll(methodParameters);

            return objectFactory.createWith(tmp);
        }

    }

    private Map<String, Object> readMethodParameters(Method method, Object[] args) {
        final Map<String, Object> methodParameters = new LinkedHashMap<>();
        final Parameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            final Parameter param = params[i];
            final Param annotation = param.getAnnotation(Param.class);
            if (annotation == null) {
                if (param.isNamePresent()) {
                    methodParameters.put(param.getName(), args[i]);
                } else {
                    throw new IllegalArgumentException("Cannot recognize parameter name. Please use @Param annotation");
                }
            } else {
                for (String name : annotation.value()) {
                    methodParameters.put(name, args[i]);
                }
            }
        }
        return methodParameters;
    }

    private T createChildProxyInstance(Map<String, Object> paramsMap) {
        return proxyFactory.newInstance(() -> {
            final RecordoObjectFactoryInvocationHandler<T> handler = new RecordoObjectFactoryInvocationHandler<>(proxyFactory, context);
            handler.handlerParameters.putAll(handlerParameters);
            handler.handlerParameters.putAll(paramsMap);
            return handler;
        });
    }

}
