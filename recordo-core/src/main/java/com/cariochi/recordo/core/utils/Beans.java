package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

@RequiredArgsConstructor(staticName = "of")
@Accessors(fluent = true)
public class Beans {

    private final ExtensionContext context;

    @Getter
    private final AnnotatedBeans annotatedBeans = new AnnotatedBeans();

    @Getter
    private final SpringBeans springBeans = new SpringBeans();

    public <T> OptionalBean<T> find(String name, Class<T> beanClass) {
        return annotatedBeans().find(name, beanClass)
                .or(() -> springBeans().find(name, beanClass));
    }

    public <T> OptionalBean<T> findByType(Class<T> beanClass) {
        return annotatedBeans().findByType(beanClass)
                .or(() -> springBeans().findByType(beanClass));
    }

    @RequiredArgsConstructor
    private abstract static class AbstractBeans {

        public <T> OptionalBean<T> findByType(Class<T> beanClass) {
            final Map<String, T> beans = getBeans(beanClass);
            return beans.size() == 1
                    ? beans.entrySet().stream().map(e -> new OptionalBean<>(e.getKey(), e.getValue(), beans.keySet())).findFirst().orElseThrow()
                    : OptionalBean.empty();
        }

        public <T> OptionalBean<T> find(String name, Class<T> beanClass) {

            if (isEmpty(name)) {
                return findByType(beanClass);
            }

            final Map<String, T> beans = getBeans(beanClass);
            return new OptionalBean<>(name, beans.get(name), beans.keySet());
        }

        protected abstract <T> Map<String, T> getBeans(Class<T> beanClass);

    }

    public class AnnotatedBeans extends AbstractBeans {

        @Override
        protected <T> Map<String, T> getBeans(Class<T> beanClass) {
            return reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                    .withTypeAndAnnotation(beanClass, EnableRecordo.class).stream()
                    .collect(toMap(JavaField::getName, JavaField::getValue));
        }

    }

    public class SpringBeans extends AbstractBeans {

        @Override
        protected <T> Map<String, T> getBeans(Class<T> beanClass) {
            try {
                return getApplicationContext(context).getBeansOfType(beanClass);
            } catch (Exception | NoClassDefFoundError e) {
                return emptyMap();
            }
        }

        public <T> void register(String beanName, Class<T> beanClass, Object... constructorArgs) {
            try {
                final ApplicationContext applicationContext = getApplicationContext(context);
                ((GenericApplicationContext) applicationContext).registerBean(beanName, beanClass, constructorArgs);
            } catch (NoClassDefFoundError e) {
                //
            }
        }

    }

    @Value
    @RequiredArgsConstructor
    public static class OptionalBean<T> {

        @Getter
        String name;

        T value;

        Set<String> availableBeanNames;

        public static <V> OptionalBean<V> empty() {
            return new OptionalBean<>(null, null, emptySet());
        }

        public <V> OptionalBean<V> map(Function<T, V> mapper) {
            return new OptionalBean<>(name, value == null ? null : mapper.apply(value), availableBeanNames);
        }

        public OptionalBean<T> or(Supplier<OptionalBean<T>> supplier) {
            return value != null ? this : supplier.get();
        }

        public Optional<T> value() {
            return Optional.ofNullable(value);
        }

    }

}
