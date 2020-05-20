package com.cariochi.recordo.interceptor;

import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RandomDataGenerator {

    private final PodamFactory factory = new PodamFactoryImpl(new DataProviderStrategy());

    public Object generateObject(Type type) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            return factory.manufacturePojo(
                    (Class<?>) parameterizedType.getRawType(),
                    parameterizedType.getActualTypeArguments()
            );
        } else if (type instanceof Class) {
            return factory.manufacturePojo((Class<?>) type);
        } else {
            return null;
        }
    }

    public static class DataProviderStrategy extends AbstractRandomDataProviderStrategy {
        public DataProviderStrategy() {
            super(2);
            setMaxDepth(3);
        }
    }
}
