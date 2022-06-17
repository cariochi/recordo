package com.cariochi.recordo.read;

import com.cariochi.recordo.core.Extension;
import com.cariochi.recordo.core.json.JsonConverter;
import com.cariochi.recordo.core.utils.ObjectReader;
import com.cariochi.reflecto.fields.JavaField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.cariochi.recordo.core.json.JsonConverters.getJsonConverter;
import static com.cariochi.reflecto.Reflecto.reflect;


public class ReadExtension implements Extension, BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).fields().includeEnclosing()
                .withAnnotation(Read.class)
                .forEach(this::processRead);
    }

    public void processRead(JavaField field) {
        final String file = field.findAnnotation(Read.class).map(Read::value).orElseThrow();
        final Type parameterType = field.getGenericType();
        final JsonConverter jsonConverter = getJsonConverter(field.getTarget());
        final ObjectReader objectReader = new ObjectReader(jsonConverter);
        if (ObjectFactory.class.isAssignableFrom(field.getType())) {
            final Type actualTypeArgument = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            field.setValue(new ObjectFactory<>(objectReader, file, actualTypeArgument));
        } else if (ObjectTemplate.class.isAssignableFrom(field.getType())) {
            final Type actualTypeArgument = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
            field.setValue(new ObjectTemplate<>(objectReader, file, actualTypeArgument));
        } else {
            field.setValue(objectReader.read(file, parameterType));
        }
    }

}
