package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.utils.Beans;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.isRequestType;
import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.isResponseType;

public abstract class AbstractMockMvcExtension implements Extension, ParameterResolver {

    protected Object processRequest(Request<Object> request,
                                    Class<? extends RequestInterceptor>[] interceptors,
                                    ParameterContext parameter,
                                    ExtensionContext context) {
        final Optional<RequestInterceptor> bean = Beans.of(context).findByType(RequestInterceptor.class);
        if (bean.isPresent()) {
            request = (Request<Object>) bean.get().apply(request);
        }
        request = intercept(interceptors, request);
        return executeRequest(request, parameter);
    }

    @SneakyThrows
    private Request<Object> intercept(Class<? extends RequestInterceptor>[] interceptors, Request<Object> request) {
        for (Class<? extends RequestInterceptor> type : interceptors) {
            final RequestInterceptor interceptor = type.getConstructor().newInstance();
            request = (Request<Object>) interceptor.apply(request);
        }
        return request;
    }

    private Object executeRequest(Request<Object> request, ParameterContext parameter) {
        final Class<?> parameterClass = parameter.getParameter().getType();
        if (isRequestType(parameterClass)) {
            return request;
        } else if (isResponseType(parameterClass)) {
            return request.perform();
        } else {
            return request.perform().getBody();
        }
    }

}
