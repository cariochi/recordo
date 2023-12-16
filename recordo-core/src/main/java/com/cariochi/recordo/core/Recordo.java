package com.cariochi.recordo.core;

import java.util.List;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

import static java.util.ServiceLoader.load;

@UtilityClass
public class Recordo {

    private static final List<ObjectCreator> creators = load(ObjectCreator.class).stream()
            .map(Provider::get)
            .collect(Collectors.toList());

    public static <T> T create(Class<T> type) {
        return creators.stream()
                .filter(c -> c.isSupported(type))
                .map(c -> c.create(type, RecordoExtension.getContext()))
                .findFirst()
                .orElseThrow();
    }

}
