package com.cariochi.recordo.read;

import com.cariochi.objecto.Modifier;
import com.cariochi.objecto.Objecto;
import com.cariochi.objecto.modifiers.ObjectoModifier;
import com.cariochi.objecto.proxy.ObjectModifier;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.methods.ReflectoMethod;
import com.cariochi.reflecto.methods.TargetMethod;
import com.cariochi.reflecto.parameters.ReflectoParameter;
import com.cariochi.reflecto.parameters.ReflectoParameters;
import com.cariochi.reflecto.proxy.InvocationHandler;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.proxy;

public class ObjectFactoryProxyHandler<T> implements InvocationHandler {

    private final Class<T> targetClass;
    private final ExtensionContext context;
    private final Object objecto;

    public ObjectFactoryProxyHandler(Class<T> targetClass, ExtensionContext context) {
        this(targetClass, context, Objecto.create(targetClass));
    }

    public ObjectFactoryProxyHandler(Class<T> targetClass, ExtensionContext context, Object objecto) {
        this.targetClass = targetClass;
        this.context = context;
        this.objecto = objecto;
    }

    @Override
    public Object invoke(Object proxy, ReflectoMethod thisMethod, Object[] args, TargetMethod proceed) {

        if (proceed != null) {
            return Object.class.equals(thisMethod.declaringType().actualType())
                    ? proceed.invoke(args)
                    : invokeObjecto(thisMethod, args);
        }

        if (thisMethod.returnType().equals(thisMethod.declaringType())) {
            final Object newObjecto = invokeObjecto(thisMethod, args);
            return proxy(targetClass)
                    .with(() -> new ObjectFactoryProxyHandler<>(targetClass, context, newObjecto))
                    .getConstructor()
                    .newInstance();
        } else {
            return thisMethod.annotations().find(Read.class)
                    .map(read -> {
                        final JsonConverter jsonConverter = JsonConverters.getJsonConverter(read.objectMapper(), context);
                        final Function<Type, Object> generator = type -> invokeObjecto(thisMethod, args);
                        final ObjectReader objectReader = new ObjectReader(jsonConverter, generator);
                        final ObjectFactory<?> objectFactory = new ObjectFactory<>(objectReader, read.value(), thisMethod.returnType());
                        Object instance = objectFactory.create();
                        instance = ((ObjectModifier) objecto).modifyObject(instance);
                        return ObjectoModifier.modifyObject(instance, readMethodParameters(thisMethod.parameters(), args));
                    })
                    .orElseGet(() -> invokeObjecto(thisMethod, args));
        }

    }

    @SneakyThrows
    private Object invokeObjecto(ReflectoMethod method, Object[] args) {
        return method.withTarget(objecto).invoke(args);
    }

    private Map<String, Object[]> readMethodParameters(ReflectoParameters parameters, Object[] args) {
        final Map<String, Object[]> methodParameters = new LinkedHashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            final ReflectoParameter param = parameters.get(i);
            final Modifier modifierParameter = param.annotations().find(Modifier.class).orElse(null);
            if (modifierParameter == null) {
                if (param.isNamePresent()) {
                    methodParameters.put(param.name(), Stream.of(args[i]).toArray());
                } else {
                    throw new IllegalArgumentException("Cannot recognize parameter name. Please use @Modifier annotation");
                }
            } else {
                for (String value : modifierParameter.value()) {
                    methodParameters.put(value, Stream.of(args[i]).toArray());
                }
            }
        }
        return methodParameters;
    }
}
