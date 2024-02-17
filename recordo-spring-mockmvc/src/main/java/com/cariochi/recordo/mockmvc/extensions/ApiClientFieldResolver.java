package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.SpringExtension;
import com.cariochi.reflecto.fields.TargetField;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;

@Deprecated(forRemoval = true)
public class ApiClientFieldResolver implements SpringExtension, BeforeEachCallback {

    private final ApiClientCreator apiClientCreator = new ApiClientCreator();

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).includeEnclosing().fields().stream()
                .filter(field -> field.annotations().contains(EnableRecordo.class))
                .filter(field -> apiClientCreator.isSupported(field.type()))
                .forEach(field -> createRecordoClient(context, field));
    }

    private void createRecordoClient(ExtensionContext context, TargetField field) {
        final Object recordoClient = apiClientCreator.create(field.type(), context);
        field.setValue(recordoClient);
    }

}
