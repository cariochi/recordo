package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

@UtilityClass
public class BeanUtils {

    public static <T> Optional<T> findBean(Class<T> aClass, ExtensionContext context) {
        final Map<String, T> beans = findBeans(aClass, context);
        return beans.values().stream().findFirst();
    }

    public static <T> Map<String, T> findBeans(Class<T> aClass, ExtensionContext context) {
        Map<String, T> beans = new HashMap<>();
        beans.putAll(findBeansInSpringContext(aClass, context));
        beans.putAll(findAnnotatedFields(aClass, context));
        return beans;
    }

    private static <T> Map<String, T> findAnnotatedFields(Class<T> aClass, ExtensionContext context) {
        return reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                .withTypeAndAnnotation(aClass, EnableRecordo.class).stream()
                .collect(toMap(JavaField::getName, JavaField::getValue));
    }

    private static <T> Map<String, T> findBeansInSpringContext(Class<T> beanClass, ExtensionContext context) {
        try {
            return getApplicationContext(context).getBeansOfType(beanClass);
        } catch (Exception | NoClassDefFoundError e) {
            return emptyMap();
        }
    }

}
