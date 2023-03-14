package com.cariochi.recordo.core.utils;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.reflecto.fields.JavaField;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static com.cariochi.reflecto.Reflecto.reflect;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

@UtilityClass
public class BeanUtils {

    public static <T> Optional<T> findBean(Class<T> aClass, ExtensionContext context) {
        return findAnnotatedField(aClass, context)
                .or(() -> findInSpringContext(aClass, context));
    }

    private static <T> Optional<T> findAnnotatedField(Class<T> aClass, ExtensionContext context) {
        return reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                .withTypeAndAnnotation(aClass, EnableRecordo.class).stream().findAny()
                .map(JavaField::getValue);
    }

    private static <T> Optional<T> findInSpringContext(Class<T> beanClass, ExtensionContext context) {
        try {
            return Optional.of(getApplicationContext(context).getBean(beanClass));
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

}
