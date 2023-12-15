package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.SpringExtension;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.utils.MockMvcUtils;
import java.lang.reflect.Type;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ParameterResolver;

public interface MockMvcParameterResolver extends SpringExtension, ParameterResolver {

    default Object processRequest(Request<Object> request, Type responseType, Class<? extends RequestInterceptor>[] interceptors) {

        for (Class<? extends RequestInterceptor> interceptorClass : interceptors) {
            final RequestInterceptor interceptor = createRequestInterceptor(interceptorClass);
            request = (Request<Object>) interceptor.apply(request);
        }

        return MockMvcUtils.getResponse(request, responseType);
    }

    @SneakyThrows
    default RequestInterceptor createRequestInterceptor(Class<? extends RequestInterceptor> interceptorClass) {
        return interceptorClass.getConstructor().newInstance();
    }

}
