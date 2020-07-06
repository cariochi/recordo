package com.cariochi.recordo.given;

import com.cariochi.recordo.RecordoError;
import com.cariochi.recordo.json.JsonConverters;
import com.cariochi.recordo.utils.exceptions.Exceptions;
import com.cariochi.recordo.utils.exceptions.ExceptionsCollector;
import com.cariochi.recordo.utils.reflection.Fields;
import com.cariochi.recordo.utils.reflection.TargetField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

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
        final Object value = new GivenObjectProvider(JsonConverters.find(field.getTarget()))
                .get(annotation.value(), field.getGenericType());
        field.setValue(value);
    }
}
