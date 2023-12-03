package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.utils.MockMvcUtils;
import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.fields.JavaField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getResponseType;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

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
    private Object processApiCall(RecordoMockMvc recordoMockMvc, Method method, Object[] args) {

        final RequestInfo baseRequestInfo = extractInfoFromClass(method.getDeclaringClass());
        final RequestInfo requestInfo = extractInfoFromMethod(method).applyBaseInfo(baseRequestInfo);

        final Type methodReturnType = method.getGenericReturnType();
        final HttpStatus expectedStatus = extractExpectedStatus(method);

        final Map<ParamType, List<Argument>> arguments = mapToArguments(method.getParameters(), args).stream()
                .filter(a -> a.getValue() != null)
                .collect(groupingBy(Argument::getParamType, toList()));

        final List<Argument> headerArguments = arguments.getOrDefault(ParamType.HEADER, emptyList());
        final List<Argument> pathVarArguments = arguments.getOrDefault(ParamType.PATH_VAR, emptyList());
        final List<Argument> parameterArguments = arguments.getOrDefault(ParamType.PARAMETER, emptyList());
        final List<Argument> fileArguments = arguments.getOrDefault(ParamType.FILE, emptyList());
        final List<Argument> bodyArguments = arguments.getOrDefault(ParamType.BODY, emptyList());
        final List<Argument> otherArguments = arguments.getOrDefault(ParamType.OTHER, emptyList());

        final Map<String, String> headers = new HashMap<>(requestInfo.getHeaders());
        headers.putAll(getHeaders(headerArguments));

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>(requestInfo.getParams());
        params.putAll(getRequestParams(parameterArguments));
        params.putAll(getParamsFromObjects(otherArguments));

        final List<File> files = new ArrayList<>();
        files.addAll(getFiles(fileArguments));
        files.addAll(getFilesFromObjects(otherArguments));

        Request<Object> request = recordoMockMvc.request(requestInfo.getHttpMethod(), requestInfo.getPath(), getResponseType(methodReturnType))
                .expectedStatus(expectedStatus)
                .uriVars(pathVarArguments.stream().map(Argument::getValue).toArray())
                .headers(headers)
                .params(params)
                .files(files)
                .body(bodyArguments.stream().findFirst().map(Argument::getValue).orElse(null));

        for (RequestInterceptor interceptor : requestInterceptors) {
            request = (Request<Object>) interceptor.apply(request);
        }

        return MockMvcUtils.getResponse(request, methodReturnType);
    }

    private HttpStatus extractExpectedStatus(Method method) {
        return Optional.ofNullable(method.getAnnotation(ResponseStatus.class))
                .map(a -> Stream.of(a.value(), a.code())
                        .filter(c -> !INTERNAL_SERVER_ERROR.equals(c))
                        .findFirst()
                        .orElse(INTERNAL_SERVER_ERROR)
                )
                .orElse(OK);
    }

    private static RequestInfo extractInfoFromClass(Class<?> type) {
        return Optional.<RequestInfo>empty()
                .or(() -> Optional.ofNullable(type.getAnnotation(RequestMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(type.getAnnotation(GetMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(type.getAnnotation(PostMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(type.getAnnotation(PutMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(type.getAnnotation(PatchMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(type.getAnnotation(DeleteMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .orElseThrow();
    }

    private static RequestInfo extractInfoFromMethod(Method method) {
        return Optional.<RequestInfo>empty()
                .or(() -> Optional.ofNullable(method.getAnnotation(RequestMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(method.getAnnotation(GetMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(method.getAnnotation(PostMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(method.getAnnotation(PutMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(method.getAnnotation(PatchMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> Optional.ofNullable(method.getAnnotation(DeleteMapping.class)).map(RequestInfoMapper::mapToRequestInfo))
                .orElseThrow();
    }

    private List<Argument> mapToArguments(Parameter[] parameters, Object[] arguments) {
        final List<Argument> argumentList = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            argumentList.add(new Argument(parameters[i].getType(), parameters[i].getAnnotations(), arguments[i]));
        }
        return argumentList;
    }

    private Map<String, String> getHeaders(List<Argument> arguments) {
        final Map<String, String> headers = new LinkedHashMap<>();
        for (Argument argument : arguments) {
            argument.findAnnotation(RequestHeader.class).ifPresent(requestHeader -> {
                final String name = Optional.of(requestHeader.value()).filter(StringUtils::isNotEmpty).orElseGet(requestHeader::name);
                headers.put(name, argument.getValue().toString());
            });
        }
        return headers;
    }

    private MultiValueMap<String, String> getRequestParams(List<Argument> arguments) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Argument argument : arguments) {
            if (Pageable.class.isAssignableFrom(argument.getType())) {
                Pageable pageable = (Pageable) argument.getValue();
                params.put("page", List.of(String.valueOf(pageable.getPageNumber())));
                params.put("size", List.of(String.valueOf(pageable.getPageSize())));
                final Sort sort = pageable.getSort();
                if (sort.isSorted()) {
                    final List<String> orders = sort.stream().map(order -> order.getProperty() + "," + order.getDirection()).collect(toList());
                    params.put("sort", orders);
                }
            } else {
                argument.findAnnotation(RequestParam.class).ifPresent(requestParam -> {
                    final String name = Optional.of(requestParam.value()).filter(StringUtils::isNotEmpty).orElseGet(requestParam::name);
                    List<String> value;
                    if (Collection.class.isAssignableFrom(argument.getType())) {
                        Collection<?> collection = (Collection<?>) argument.getValue();
                        value = collection.stream().map(Object::toString).collect(toList());
                    } else {
                        value = List.of(argument.getValue().toString());
                    }
                    params.put(name, value);
                });

            }
        }
        return params;
    }

    private MultiValueMap<String, String> getParamsFromObjects(List<Argument> arguments) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Argument arg : arguments) {
            final Object dto = arg.getValue();
            final List<JavaField> fields = Reflecto.reflect(dto).fields().all();
            for (JavaField field : fields) {
                if (File.class.isAssignableFrom(field.getType()) || MultipartFile.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                List<String> value;
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Collection<?> collection = field.getValue();
                    value = collection.stream().map(Object::toString).collect(toList());
                } else {
                    value = List.of(field.getValue().toString());
                }
                params.put(field.getName(), value);
            }
        }
        return params;
    }

    @SneakyThrows
    private List<File> getFiles(List<Argument> arguments) {
        final List<File> files = new ArrayList<>();
        for (Argument argument : arguments) {
            if (File.class.isAssignableFrom(argument.getType())) {
                files.add((File) argument.getValue());
            } else if (MultipartFile.class.isAssignableFrom(argument.getType())) {
                final MultipartFile multipartFile = (MultipartFile) argument.getValue();
                files.add(File.builder()
                        .name(multipartFile.getName())
                        .contentType(multipartFile.getContentType())
                        .originalFilename(multipartFile.getOriginalFilename())
                        .content(multipartFile.getBytes())
                        .build()
                );
            }
        }
        return files;
    }

    @SneakyThrows
    private List<File> getFilesFromObjects(List<Argument> arguments) {
        final List<File> files = new ArrayList<>();
        for (Argument arg : arguments) {
            final Object dto = arg.getValue();
            final List<JavaField> fields = Reflecto.reflect(dto).fields().all();
            for (JavaField field : fields) {
                if (File.class.isAssignableFrom(field.getType())) {
                    files.add(field.getValue());
                } else if (MultipartFile.class.isAssignableFrom(field.getType())) {
                    final MultipartFile multipartFile = field.getValue();
                    files.add(File.builder()
                            .name(multipartFile.getName())
                            .contentType(multipartFile.getContentType())
                            .originalFilename(multipartFile.getOriginalFilename())
                            .content(multipartFile.getBytes())
                            .build());
                }
            }
        }
        return files;
    }

    @Value
    private static class Argument {

        Class<?> type;
        Annotation[] annotations;
        Object value;

        public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
            return findAnnotation(annotationClass).isPresent();
        }

        public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
            return Stream.of(annotations)
                    .filter(annotationClass::isInstance)
                    .map(annotationClass::cast)
                    .findFirst();
        }

        public ParamType getParamType() {
            if (hasAnnotation(RequestHeader.class)) {
                return ParamType.HEADER;
            } else if (hasAnnotation(PathVariable.class)) {
                return ParamType.PATH_VAR;
            } else if (hasAnnotation(RequestParam.class) || Pageable.class.isAssignableFrom(type)) {
                return File.class.isAssignableFrom(type) || MultipartFile.class.isAssignableFrom(type) ? ParamType.FILE : ParamType.PARAMETER;
            } else if (hasAnnotation(RequestBody.class)) {
                return ParamType.BODY;
            }
            return ParamType.OTHER;
        }

    }

    private enum ParamType {
        HEADER,
        PATH_VAR,
        PARAMETER,
        FILE,
        BODY,
        OTHER
    }

}
