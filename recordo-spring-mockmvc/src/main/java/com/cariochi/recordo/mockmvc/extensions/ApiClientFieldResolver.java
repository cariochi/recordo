package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.SpringExtension;
import com.cariochi.reflecto.fields.JavaField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;

@Deprecated(forRemoval = true)
public class ApiClientFieldResolver implements SpringExtension, BeforeEachCallback {

    private final ApiClientCreator apiClientCreator = new ApiClientCreator();

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).fields().includeEnclosing().withAnnotation(EnableRecordo.class).stream()
                .filter(field -> apiClientCreator.isSupported(field.getType()))
                .forEach(field -> createRecordoClient(context, field));
    }

    private void createRecordoClient(ExtensionContext context, JavaField field) {
        final Object recordoClient = apiClientCreator.create(field.getType(), context);
        field.setValue(recordoClient);
    }

}
