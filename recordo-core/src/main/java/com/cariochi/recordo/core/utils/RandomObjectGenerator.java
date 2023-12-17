package com.cariochi.recordo.core.utils;

import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.internal.ApiImpl;
import org.instancio.settings.Keys;
import org.instancio.settings.Settings;

@Slf4j
public class RandomObjectGenerator {

    private static final Settings SETTINGS = Settings.create()
            .set(Keys.MAX_DEPTH, 6)
            .set(Keys.COLLECTION_NULLABLE, false)
            .set(Keys.MAP_NULLABLE, false)
            .set(Keys.ARRAY_ELEMENTS_NULLABLE, false)
            .set(Keys.COLLECTION_ELEMENTS_NULLABLE, false)
            .set(Keys.BIG_DECIMAL_SCALE, 2)
            .set(Keys.ARRAY_MAX_LENGTH, 3)
            .set(Keys.COLLECTION_MAX_SIZE, 3)
            .set(Keys.MAP_MAX_SIZE, 3);

    public Object generateInstance(Type type) {
        final Model<Object> model = new ApiImpl<>(type)
                .withSettings(SETTINGS)
                .toModel();

        return Instancio.create(model);
    }

}
