package com.cariochi.recordo.core.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import static java.math.RoundingMode.HALF_UP;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDate.now;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@Slf4j
public class RandomObjectGenerator {

    public Object generateInstance(Type type, int depth) {
        try {
            final EasyRandom random = easyRandom(depth);
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                final Object parentObject = random.nextObject(rawType);
                if (parentObject instanceof Collection) {
                    IntStream.range(0, 3).forEach(i -> {
                        final Object value = generateInstance(parameterizedType.getActualTypeArguments()[0], depth);
                        ((Collection<Object>) parentObject).add(value);
                    });
                } else if (parentObject instanceof Map) {
                    IntStream.range(0, 3).forEach(i -> {
                        final Object key = generateInstance(parameterizedType.getActualTypeArguments()[0], depth);
                        final Object value = generateInstance(parameterizedType.getActualTypeArguments()[1], depth);
                        ((Map<Object, Object>) parentObject).put(key, value);
                    });
                }
                return parentObject;
            } else if (type instanceof Class) {
                return random.nextObject((Class) type);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static EasyRandom easyRandom(int depth) {
        return new EasyRandom(parameters(depth));
    }

    private static EasyRandomParameters parameters(int depth) {
        return new EasyRandomParameters()
                .stringLengthRange(3, 8)
                .collectionSizeRange(2, 5)
                .randomizationDepth(depth)
                .scanClasspathForConcreteTypes(true)
                .ignoreRandomizationErrors(true)
                .dateRange(now().minusMonths(1), now())
                .charset(UTF_8)
                .randomize(String.class, () -> LoremIpsum.generateWords(1, 3))
                .randomize(Integer.class, () -> nextInt(1, 1000))
                .randomize(Long.class, () -> nextLong(1, 1000))
                .randomize(Double.class, () -> BigDecimal.valueOf(nextDouble(1, 1000)).setScale(2, HALF_UP).doubleValue())
                .randomize(Float.class, () -> BigDecimal.valueOf(nextDouble(1, 1000)).setScale(2, HALF_UP).floatValue())
                .randomize(BigDecimal.class, () -> BigDecimal.valueOf(nextDouble(1, 1000)).setScale(2, HALF_UP))
                .objectPoolSize(1000);
    }

}
