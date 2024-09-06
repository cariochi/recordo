package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import com.cariochi.recordo.mockmvc.Request;
import com.cariochi.recordo.mockmvc.Request.File;
import com.cariochi.recordo.mockmvc.RequestInterceptor;
import com.cariochi.recordo.mockmvc.utils.MockMvcUtils;
import com.cariochi.reflecto.Reflecto;
import com.cariochi.reflecto.base.ReflectoAnnotations;
import com.cariochi.reflecto.fields.TargetField;
import com.cariochi.reflecto.methods.ReflectoMethod;
import com.cariochi.reflecto.methods.TargetMethod;
import com.cariochi.reflecto.parameters.ReflectoParameter;
import com.cariochi.reflecto.parameters.ReflectoParameters;
import com.cariochi.reflecto.proxy.InvocationHandler;
import com.cariochi.reflecto.types.ReflectoType;
import java.lang.annotation.Annotation;
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
import org.apache.commons.lang3.StringUtils;
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
public class ApiClientProxyHandler implements InvocationHandler {

    private final RecordoMockMvc recordoMockMvc;
    private final List<RequestInterceptor> requestInterceptors;

    @Override
    public Object invoke(Object proxy, ReflectoMethod method, Object[] args, TargetMethod proceed) {

        if (Object.class.equals(method.declaringType().actualClass())) {
            return proceed.invoke(args);
        }

        final RequestInfo baseRequestInfo = extractInfoFromClass(method.declaringType());
        final RequestInfo requestInfo = extractInfoFromMethod(method).applyBaseInfo(baseRequestInfo);

        final ReflectoType methodReturnType = method.returnType();
        final HttpStatus expectedStatus = extractExpectedStatus(method);

        final Map<ParamType, List<Argument>> arguments = mapToArguments(method.parameters(), args).stream()
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

        Request<Object> request = recordoMockMvc.request(requestInfo.getHttpMethod(), requestInfo.getPath(), getResponseType(methodReturnType).actualType())
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

    private HttpStatus extractExpectedStatus(ReflectoMethod method) {
        return method.annotations().find(ResponseStatus.class)
                .map(a -> Stream.of(a.value(), a.code())
                        .filter(c -> !INTERNAL_SERVER_ERROR.equals(c))
                        .findFirst()
                        .orElse(INTERNAL_SERVER_ERROR)
                )
                .orElse(OK);
    }

    private static RequestInfo extractInfoFromClass(ReflectoType type) {
        return Optional.<RequestInfo>empty()
                .or(() -> type.annotations().find(RequestMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> type.annotations().find(GetMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> type.annotations().find(PostMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> type.annotations().find(PutMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> type.annotations().find(PatchMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> type.annotations().find(DeleteMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .orElseThrow();
    }

    private static RequestInfo extractInfoFromMethod(ReflectoMethod method) {
        return Optional.<RequestInfo>empty()
                .or(() -> method.annotations().find(RequestMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> method.annotations().find(GetMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> method.annotations().find(PostMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> method.annotations().find(PutMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> method.annotations().find(PatchMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .or(() -> method.annotations().find(DeleteMapping.class).map(RequestInfoMapper::mapToRequestInfo))
                .orElseThrow();
    }

    private List<Argument> mapToArguments(ReflectoParameters parameters, Object[] arguments) {
        final List<Argument> argumentList = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            argumentList.add(new Argument(parameters.get(i), arguments[i]));
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
            if (argument.getType().is(Pageable.class)) {
                Pageable pageable = (Pageable) argument.getValue();
                params.put("page", List.of(String.valueOf(pageable.getPageNumber())));
                params.put("size", List.of(String.valueOf(pageable.getPageSize())));
                final Sort sort = pageable.getSort();
                if (sort.isSorted()) {
                    final List<String> orders = sort.stream().map(order -> order.getProperty() + "," + order.getDirection()).toList();
                    params.put("sort", orders);
                }
            } else {
                argument.findAnnotation(RequestParam.class).ifPresent(requestParam -> {
                    final String name = Optional.of(requestParam.value())
                            .filter(StringUtils::isNotEmpty)
                            .or(() -> Optional.of(requestParam.name()).filter(StringUtils::isNotEmpty))
                            .or(() -> Optional.of(argument).map(Argument::getParameter).filter(ReflectoParameter::isNamePresent).map(ReflectoParameter::name))
                            .orElseThrow(() -> new IllegalArgumentException("Cannot recognize @RequestParam name"));

                    List<String> value;
                    if (argument.getType().is(Collection.class)) {
                        Collection<?> collection = (Collection<?>) argument.getValue();
                        value = collection.stream().map(Object::toString).toList();
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
            final List<TargetField> fields = Reflecto.reflect(dto).fields().list();
            for (TargetField field : fields) {
                if (field.getValue() == null) {
                    continue;
                }
                if (field.type().is(File.class) || field.type().is(MultipartFile.class)) {
                    continue;
                }
                List<String> value;
                if (field.type().is(Collection.class)) {
                    Collection<?> collection = field.getValue();
                    value = collection.stream().map(Object::toString).toList();
                } else {
                    value = List.of(field.getValue().toString());
                }
                params.put(field.name(), value);
            }
        }
        return params;
    }

    @SneakyThrows
    private List<File> getFiles(List<Argument> arguments) {
        final List<File> files = new ArrayList<>();
        for (Argument argument : arguments) {
            if (argument.getType().is(File.class)) {
                files.add((File) argument.getValue());
            } else if (argument.getType().is(MultipartFile.class)) {
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
            final List<TargetField> fields = Reflecto.reflect(dto).fields().list();
            for (TargetField field : fields) {
                if (field.getValue() == null) {
                    continue;
                }
                if (field.type().is(File.class)) {
                    files.add(field.getValue());
                } else if (field.type().is(MultipartFile.class)) {
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

        ReflectoParameter parameter;
        Object value;

        public ReflectoType getType() {
            return parameter.type();
        }

        public <T extends Annotation> Optional<T> findAnnotation(Class<T> annotationClass) {
            return parameter.annotations().find(annotationClass);
        }

        public ParamType getParamType() {
            final ReflectoAnnotations annotations = parameter.annotations();
            if (annotations.contains(RequestHeader.class)) {
                return ParamType.HEADER;
            } else if (annotations.contains(PathVariable.class)) {
                return ParamType.PATH_VAR;
            } else if (annotations.contains(RequestParam.class) || getType().is(Pageable.class)) {
                return getType().is(File.class) || getType().is(MultipartFile.class) ? ParamType.FILE : ParamType.PARAMETER;
            } else if (annotations.contains(RequestBody.class)) {
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
