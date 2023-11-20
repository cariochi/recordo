package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static com.cariochi.reflecto.Reflecto.reflect;
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

    public <T> Optional<T> find(String name, Class<T> beanClass) {
        return annotatedBeans().find(name, beanClass)
                .or(() -> springBeans().find(name, beanClass));
    }

    public <T> Optional<T> findByType(Class<T> beanClass) {
        return annotatedBeans().findByType(beanClass)
                .or(() -> springBeans().findByType(beanClass));
    }

    public class AnnotatedBeans {

        public <T> Optional<T> find(String name, Class<T> beanClass) {

            if (isEmpty(name)) {
                return findByType(beanClass);
            }

            return getFields(beanClass).stream()
                    .filter(javaField -> name.equals(javaField.getName()))
                    .map(JavaField::getValue)
                    .map(beanClass::cast)
                    .findFirst();
        }

        public <T> Optional<T> findByType(Class<T> beanClass) {
            final List<JavaField> fields = getFields(beanClass);
            return fields.size() == 1
                    ? Optional.of(fields.iterator().next()).map(JavaField::getValue)
                    : Optional.empty();
        }

        private <T> List<JavaField> getFields(Class<T> beanClass) {
            return reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                    .withTypeAndAnnotation(beanClass, EnableRecordo.class);
        }

    }

    public class SpringBeans {

        public <T> Optional<T> find(String name, Class<T> beanClass) {

            if (isEmpty(name)) {
                return findByType(beanClass);
            }

            try {
                return Optional.of(getApplicationContext(context).getBean(name, beanClass));
            } catch (Exception | NoClassDefFoundError e) {
                return Optional.empty();
            }
        }

        public <T> Optional<T> findByType(Class<T> beanClass) {
            try {
                return Optional.of(getApplicationContext(context).getBean(beanClass));
            } catch (Exception | NoClassDefFoundError e) {
                return Optional.empty();
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

}
