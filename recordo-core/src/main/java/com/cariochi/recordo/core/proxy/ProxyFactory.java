package com.cariochi.recordo.core.proxy;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static java.lang.invoke.MethodType.methodType;

@RequiredArgsConstructor(staticName = "of")
public class ProxyFactory<T> {

    private final Class<T> targetInterface;

    public T newInstance(Supplier<InvocationHandler> handler) {
        return (T) Proxy.newProxyInstance(
                ProxyFactory.class.getClassLoader(),
                new Class[]{targetInterface},
                new ProxyInvocationHandler(handler.get())
        );
    }

    @RequiredArgsConstructor
    private static class ProxyInvocationHandler implements InvocationHandler {

        private final InvocationHandler handler;

        @SneakyThrows
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {

            if (method.isDefault()) {
                return invokeDefaultMethod(proxy, method, args);
            } else if (method.getDeclaringClass().equals(Object.class)) {
                return invokeObjectMethod(proxy, method, args);
            } else {
                return handler.invoke(proxy, method, args);
            }

        }

        @SneakyThrows
        private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) {
            final MethodType methodType = methodType(method.getReturnType(), method.getParameterTypes());
            return MethodHandles.lookup()
                    .findSpecial(method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass())
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }

        @SneakyThrows
        private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == proxy) {
                        args[i] = this;
                    }
                }
            }
            return method.invoke(this, args);
        }

    }

}
