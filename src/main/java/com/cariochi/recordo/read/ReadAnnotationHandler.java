package com.cariochi.recordo.read;

import com.cariochi.recordo.Read;
import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.exceptions.Exceptions;
import com.cariochi.recordo.utils.exceptions.ExceptionsCollector;
import com.cariochi.reflecto.fields.JavaField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.cariochi.reflecto.Reflecto.reflect;


public class ReadAnnotationHandler implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        try (final ExceptionsCollector exceptionsCollector = Exceptions.collectorOf(RecordoError.class)) {
            reflect(context.getRequiredTestInstance()).fields()
                    .withAnnotation(Read.class)
                    .forEach(exceptionsCollector.consuming(this::processRead));
        }
    }

    public void processRead(JavaField field) {
        final String file = field.findAnnotation(Read.class).map(Read::value).orElseThrow();
        final Type parameterType = field.getGenericType();
        final JsonConverter jsonConverter = JsonConverters.find(field.getTarget());
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
