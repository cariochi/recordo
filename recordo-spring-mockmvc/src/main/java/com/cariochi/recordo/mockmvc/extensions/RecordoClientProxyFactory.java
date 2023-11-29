package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.utils.MockMvcUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;

@RequiredArgsConstructor
public class RecordoClientProxyFactory {

    private final ProxyFactory proxyFactory = new ProxyFactory();
    private final RecordoMockMvc recordoMockMvc;
    private final List<RequestInterceptor> requestInterceptors;

    public <T> T getRecordoClient(Class<T> targetClass) {
        proxyFactory.setTargetClass(targetClass);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice((MethodInterceptor) invocation -> processApiCall(recordoMockMvc, invocation.getMethod(), invocation.getArguments()));
        return (T) proxyFactory.getProxy();
    }

    @SneakyThrows
    private Object processApiCall(RecordoMockMvc recordoMockMvc, Method method, Object[] arguments) {

        final HttpMethod httpMethod = httpMethod(method);
        final String path = path(method);
        final Type methodReturnType = method.getGenericReturnType();

        final Object[] uriVars = uriVars(method.getParameters(), arguments);
        final MultiValueMap<String, String> params = params(method.getParameters(), arguments);
        final List<File> files = files(method.getParameters(), arguments);
        final Map<String, String> headers = headers(method.getParameters(), arguments);
        final Object body = body(method.getParameters(), arguments);

        Request<Object> request = recordoMockMvc.request(httpMethod, path, getResponseType(methodReturnType))
                .uriVars(uriVars)
                .headers(headers)
                .params(params)
                .files(files)
                .body(body);

        for (RequestInterceptor interceptor : requestInterceptors) {
            request = (Request<Object>) interceptor.apply(request);
        }

        return MockMvcUtils.getResponse(request, methodReturnType);
    }

    private HttpMethod httpMethod(Method method) {
        return Optional.ofNullable(method.getAnnotation(RequestMapping.class))
                .map(RequestMapping::method).filter(ArrayUtils::isNotEmpty).map(m -> m[0]).map(m -> HttpMethod.valueOf(m.name()))
                .or(() -> Optional.ofNullable(method.getAnnotation(GetMapping.class)).map(m -> HttpMethod.GET))
                .or(() -> Optional.ofNullable(method.getAnnotation(PostMapping.class)).map(m -> HttpMethod.POST))
                .or(() -> Optional.ofNullable(method.getAnnotation(PutMapping.class)).map(m -> HttpMethod.PUT))
                .or(() -> Optional.ofNullable(method.getAnnotation(PatchMapping.class)).map(m -> HttpMethod.PATCH))
                .or(() -> Optional.ofNullable(method.getAnnotation(DeleteMapping.class)).map(m -> HttpMethod.DELETE))
                .orElseThrow();
    }

    private String path(Method method) {

        final String path = Optional.ofNullable(method.getAnnotation(RequestMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)).filter(m -> m.length > 0)
                .or(() -> Optional.ofNullable(method.getAnnotation(GetMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)))
                .or(() -> Optional.ofNullable(method.getAnnotation(PostMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)))
                .or(() -> Optional.ofNullable(method.getAnnotation(PutMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)))
                .or(() -> Optional.ofNullable(method.getAnnotation(PatchMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)))
                .or(() -> Optional.ofNullable(method.getAnnotation(DeleteMapping.class)).map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path)))
                .filter(p -> p.length > 0)
                .map(p -> p[0])
                .orElse("");

        return Optional.ofNullable(method.getDeclaringClass().getAnnotation(RequestMapping.class))
                .map(m -> Optional.of(m.value()).filter(ArrayUtils::isNotEmpty).orElseGet(m::path))
                .filter(ArrayUtils::isNotEmpty)
                .map(p -> p[0])
                .map(p -> p + path)
                .orElse(path);
    }

    private Object[] uriVars(Parameter[] parameters, Object[] arguments) {
        final List<Object> vars = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Object argument = arguments[i];
            final PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                vars.add(argument);
            }
        }
        return vars.toArray();
    }

    private Map<String, String> headers(Parameter[] parameters, Object[] arguments) {
        final Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Object argument = arguments[i];
            if (argument != null) {
                final RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
                if (requestHeader != null) {
                    final String name = Optional.of(requestHeader.value()).filter(StringUtils::isNotEmpty).orElseGet(requestHeader::name);
                    headers.put(name, argument.toString());
                }
            }
        }
        return headers;
    }

    private MultiValueMap<String, String> params(Parameter[] parameters, Object[] arguments) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Object argument = arguments[i];
            if (argument == null || argument instanceof MultipartFile) {
                continue;
            }
            if (Pageable.class.isAssignableFrom(parameter.getType())) {
                Pageable pageable = (Pageable) argument;
                params.put("page", List.of(String.valueOf(pageable.getPageNumber())));
                params.put("size", List.of(String.valueOf(pageable.getPageSize())));
                final Sort sort = pageable.getSort();
                if (sort.isSorted()) {
                    final List<String> orders = sort.stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(Collectors.toList());
                    params.put("sort", orders);
                }
            } else {
                final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    final String name = Optional.of(requestParam.value()).filter(StringUtils::isNotEmpty).orElseGet(requestParam::name);
                    params.put(name, List.of(argument.toString()));
                }
            }
        }
        return params;
    }

    @SneakyThrows
    private List<File> files(Parameter[] parameters, Object[] arguments) {
        final List<File> files = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Object argument = arguments[i];
            if (argument instanceof MultipartFile) {
                final RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam != null) {
                    final MultipartFile multipartFile = (MultipartFile) argument;
                    files.add(File.builder()
                            .name(multipartFile.getName())
                            .contentType(multipartFile.getContentType())
                            .originalFilename(multipartFile.getOriginalFilename())
                            .content(multipartFile.getBytes())
                            .build()
                    );
                }
            }
        }
        return files;
    }

    private Object body(Parameter[] parameters, Object[] arguments) {
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final Object argument = arguments[i];
            final RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                return argument;
            }
        }
        return null;
    }

}
