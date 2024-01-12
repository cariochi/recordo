package com.cariochi.recordo.read;

import com.cariochi.objecto.Modifier;
import com.cariochi.objecto.Objecto;
import com.cariochi.objecto.modifiers.ObjectoModifier;
import com.cariochi.objecto.proxy.ObjectModifier;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.json.JsonConverters;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.proxy.ProxyFactory;
import com.cariochi.reflecto.proxy.ProxyFactory.MethodHandler;
import com.cariochi.reflecto.proxy.ProxyFactory.MethodProceed;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;

public class RecordoObjectFactoryMethodHandler<T> implements MethodHandler {

  private final Class<T> targetClass;
  private final ExtensionContext context;
  private final Object objecto;

  public RecordoObjectFactoryMethodHandler(Class<T> targetClass, ExtensionContext context) {
    this(targetClass, context, Objecto.create(targetClass));
  }

  public RecordoObjectFactoryMethodHandler(Class<T> targetClass, ExtensionContext context, Object objecto) {
    this.targetClass = targetClass;
    this.context = context;
    this.objecto = objecto;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args, MethodProceed proceed) throws Throwable {

    if (Object.class.equals(method.getDeclaringClass())) {
      return proceed.proceed();
    }

    if (proceed == null) {

      if (method.getReturnType().equals(method.getDeclaringClass())) {
        final Object newObjecto = invokeObjecto(method, args);
        final RecordoObjectFactoryMethodHandler<T> handler = new RecordoObjectFactoryMethodHandler<>(targetClass, context, newObjecto);
        return ProxyFactory.createInstance(handler, targetClass);
      } else {
        return Optional.ofNullable(method.getAnnotation(Read.class))
            .map(read -> {
              final JsonConverter jsonConverter = JsonConverters.getJsonConverter(read.objectMapper(), context);
              final Function<Type, Object> generator = type -> invokeObjecto(method, args);
              final ObjectReader objectReader = new ObjectReader(jsonConverter, generator);
              final ObjectFactory<?> objectFactory = new ObjectFactory<>(objectReader, read.value(), method.getGenericReturnType());
              Object instance = objectFactory.create();
              instance = ((ObjectModifier) objecto).modifyObject(instance);
              return ObjectoModifier.modifyObject(instance, readMethodParameters(method.getParameters(), args));
            })
            .orElseGet(() -> invokeObjecto(method, args));
      }
    } else {
      return invokeObjecto(method, args);
    }
  }

  @SneakyThrows
  private Object invokeObjecto(Method method, Object[] args) {
    return method.invoke(objecto, args);
  }

  private Map<String, Object[]> readMethodParameters(Parameter[] parameters, Object[] args) {
    final Map<String, Object[]> methodParameters = new LinkedHashMap<>();
    for (int i = 0; i < parameters.length; i++) {
      final Parameter param = parameters[i];
      final Modifier modifierParameter = param.getAnnotation(Modifier.class);
      if (modifierParameter == null) {
        if (param.isNamePresent()) {
          methodParameters.put(param.getName(), Stream.of(args[i]).toArray());
        } else {
          throw new IllegalArgumentException("Cannot recognize parameter name. Please use @Modifier annotation");
        }
      } else {
        methodParameters.put(modifierParameter.value(), Stream.of(args[i]).toArray());
      }
    }
    return methodParameters;
  }

}
