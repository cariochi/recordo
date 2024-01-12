package com.cariochi.recordo.core;

import java.util.List;
import java.util.ServiceLoader.Provider;
import lombok.experimental.UtilityClass;

import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class Recordo {

    private static final List<ObjectCreator> creators = load(ObjectCreator.class).stream()
            .map(Provider::get)
            .collect(toList());

    public static <T> T create(Class<T> type) {
        return creators.stream()
                .filter(c -> c.isSupported(type))
                .map(c -> c.create(type, RecordoExtension.getContext()))
                .findFirst()
                .orElseThrow();
    }

}
