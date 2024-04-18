package com.cariochi.recordo.read;

import com.cariochi.recordo.core.RegularExtension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.fields.TargetField;
import com.cariochi.reflecto.types.ReflectoType;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.reflecto.Reflecto.reflect;


public class ReadFieldResolver implements RegularExtension, BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).includeEnclosing().fields().stream()
                .filter(field -> field.annotations().contains(Read.class))
                .forEach(field -> processRead(context, field));
    }

    public void processRead(ExtensionContext context, TargetField field) {
        final Read annotation = field.annotations().get(Read.class);
        final String file = annotation.value();
        final ReflectoType type = field.type();
        final JsonConverter jsonConverter = getJsonConverter(annotation.objectMapper(), context);
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        if (field.type().is(ObjectFactory.class)) {
            final ReflectoType typeArgument = type.arguments().get(0);
            field.setValue(new ObjectFactory<>(objectReader, file, typeArgument));
        } else {
            field.setValue(objectReader.read(file, type));
        }
    }

}
