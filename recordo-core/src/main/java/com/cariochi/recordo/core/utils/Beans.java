package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.RecordoBean;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
@Accessors(fluent = true)
public class Beans {

    private final ExtensionContext context;

    public <T> Optional<Bean<T>> find(String name, Class<T> beanClass) {
        if (isEmpty(name)) {
            return findByType(beanClass);
        }
        return findByName(name, recordoBeans(beanClass))
                .or(() -> findByName(name, springBeans(beanClass)));
    }

    public <T> Optional<Bean<T>> findByType(Class<T> beanClass) {
        return uniqueBean(recordoBeans(beanClass), beanClass)
                .or(() -> uniqueBean(springBeans(beanClass), beanClass));
    }

    public <T> List<Bean<T>> findAll(Class<T> beanClass) {
        List<Bean<T>> beans = new ArrayList<>();
        beans.addAll(recordoBeans(beanClass));
        beans.addAll(springBeans(beanClass));
        return beans;
    }

    private <T> Optional<Bean<T>> findByName(String name, List<Bean<T>> beans) {
        return beans.stream()
                .filter(bean -> bean.name().equals(name))
                .findFirst();
    }

    private <T> Optional<Bean<T>> uniqueBean(List<Bean<T>> beans, Class<T> beanClass) {
        if (beans.isEmpty()) {
            return Optional.empty();
        }

        List<Bean<T>> primaryBeans = beans.stream()
                .filter(Bean::primary)
                .toList();

        if (primaryBeans.size() == 1) {
            return primaryBeans.stream().findFirst();
        }

        if (primaryBeans.size() > 1) {
            List<String> primaryBeanNames = primaryBeans.stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple primary %s beans found: %s".formatted(beanClass.getSimpleName(), primaryBeanNames));
        }

        if (beans.size() > 1) {
            List<String> beanNames = beans.stream().map(Bean::name).toList();
            throw new IllegalStateException("Multiple %s beans found: %s".formatted(beanClass.getSimpleName(), beanNames));
        }

        return beans.stream().findFirst();

    }

    private <T> List<Bean<T>> recordoBeans(Class<T> beanClass) {
        return context.getTestInstances().stream()
                .flatMap(instances -> instances.getAllInstances().stream())
                .flatMap(instance -> reflect(instance).fields().stream()
                        .filter(field -> field.type().is(beanClass))
                        .filter(field -> field.annotations().contains(RecordoBean.class)))
                .map(field -> new Bean<T>(field.name(), field.getValue(), false, true))
                .toList();
    }

    private <T> List<Bean<T>> springBeans(Class<T> beanClass) {
        try {
            return getApplicationContext(context).getBeansOfType(beanClass).entrySet().stream()
                    .map(entry -> new Bean<>(entry.getKey(), entry.getValue(), isPrimaryBean(entry.getKey()), false))
                    .toList();
        } catch (Exception | NoClassDefFoundError e) {
            return List.of();
        }
    }

    public boolean isPrimaryBean(String beanName) {
        try {
            return getApplicationContext(context).findAnnotationOnBean(beanName, Primary.class) != null;
        } catch (Exception | NoClassDefFoundError e) {
            return false;
        }
    }

    public record Bean<T>(String name, T instance, boolean primary, boolean recordoBean) {
    }
}
