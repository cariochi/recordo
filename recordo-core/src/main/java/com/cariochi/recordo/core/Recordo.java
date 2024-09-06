package com.cariochi.recordo.core;

import com.cariochi.reflecto.types.ReflectoType;
import java.util.List;
import java.util.ServiceLoader.Provider;
import lombok.experimental.UtilityClass;

import static com.cariochi.reflecto.Reflecto.reflect;
import static java.util.ServiceLoader.load;

@UtilityClass
public class Recordo {

    private static final List<ObjectCreator> creators = load(ObjectCreator.class).stream()
            .map(Provider::get)
            .toList();

    public static <T> T create(Class<T> type) {
        final ReflectoType reflectoType = reflect(type);
        return creators.stream()
                .filter(c -> c.isSupported(reflectoType))
                .map(c -> c.create(reflectoType, RecordoExtension.getContext()))
                .findFirst()
                .map(type::cast)
                .orElseThrow();
    }

}
