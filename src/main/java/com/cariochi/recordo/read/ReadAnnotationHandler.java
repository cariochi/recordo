package com.cariochi.recordo.read;

import com.cariochi.recordo.Read;
import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverter;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.exceptions.Exceptions;
import com.cariochi.recordo.utils.exceptions.ExceptionsCollector;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Type;

public class ReadAnnotationHandler implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        try (final ExceptionsCollector exceptionsCollector = Exceptions.collectorOf(RecordoError.class)) {
            Fields.of(context.getRequiredTestInstance())
                    .withAnnotation(Read.class)
                    .forEach(exceptionsCollector.consuming(this::processRead));
        }
    }

    public void processRead(TargetField field) {
        final String file = field.getAnnotation(Read.class).value();
        final Type parameterType = field.getGenericType();
        final JsonConverter jsonConverter = JsonConverters.find(field.getTarget());
        final Object value = ObjectReader.read(file, parameterType, jsonConverter);
        field.setValue(value);
    }

}
