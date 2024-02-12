package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.TargetField;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
@Accessors(fluent = true)
public class Beans {

    private final ExtensionContext context;

    public <T> Optional<T> find(String name, Class<T> beanClass) {
        return find(name, annotatedFields(beanClass), beanClass)
                .or(() -> find(name, springBeans(beanClass), beanClass));
    }

    public <T> Optional<T> findByType(Class<T> beanClass) {
        return singleBean(annotatedFields(beanClass), beanClass)
                .or(() -> singleBean(springBeans(beanClass), beanClass));
    }

    public <T> Map<String, T> findAll(Class<T> beanClass) {
        final Map<String, T> map = new HashMap<>();
        map.putAll(springBeans(beanClass));
        map.putAll(annotatedFields(beanClass));
        return map;
    }

    private <T> Optional<T> find(String name, Map<String, T> beans, Class<T> beanClass) {
        if (isEmpty(name)) {
            return singleBean(beans, beanClass);
        }
        final T value = beans.get(name);
        if (value == null) {
            log.warn("No {} bean named '{}' available. Available beans: {}", beanClass.getSimpleName(), name, beans.keySet());
        }
        return Optional.ofNullable(value);
    }

    private <T> Optional<T> singleBean(Map<String, T> beans, Class<T> beanClass) {
        if (beans.isEmpty()) {
            return Optional.empty();
        } else {
            final Entry<String, T> e = beans.entrySet().iterator().next();
            if (beans.size() != 1) {
                log.warn("Multiple {} beans found: {}, '{}' will be used", beanClass.getSimpleName(), beans.keySet(), e.getKey());
            }
            return Optional.of(e.getValue());
        }
    }

    private <T> Map<String, T> annotatedFields(Class<T> beanClass) {
        return context.getTestInstance()
                .map(instance -> reflect(instance).includeEnclosing().fields().stream()
                        .filter(field -> field.type().is(beanClass))
                        .filter(field -> field.annotations().contains(EnableRecordo.class))
                        .collect(toMap(
                                TargetField::name,
                                f -> (T) f.getValue(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ))
                )
                .orElseGet(LinkedHashMap::new);
    }

    private <T> Map<String, T> springBeans(Class<T> beanClass) {
        try {
            return getApplicationContext(context).getBeansOfType(beanClass);
        } catch (Exception | NoClassDefFoundError e) {
            return emptyMap();
        }
    }

}
