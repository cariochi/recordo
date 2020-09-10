package com.cariochi.recordo.given;

import com.cariochi.recordo.Given;
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

public class GivenAnnotationHandler implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        final ExceptionsCollector collector = Exceptions.collectorOf(RecordoError.class);
        Fields.of(context.getRequiredTestInstance())
                .withAnnotation(Given.class)
                .forEach(collector.consuming(
                        field -> processGiven(field.getAnnotation(Given.class), field)
                ));
        if (collector.hasExceptions()) {
            throw new RecordoError(collector.getMessage());
        }
    }

    public void processGiven(Given annotation, TargetField field) {
        final Type parameterType = field.getGenericType();
        final JsonConverter jsonConverter = JsonConverters.find(field.getTarget());
        final Object value = GivenObjectReader.read(annotation.value(), parameterType, jsonConverter);
        field.setValue(value);
    }
}
