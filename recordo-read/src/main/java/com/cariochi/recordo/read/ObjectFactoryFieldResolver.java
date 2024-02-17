package com.cariochi.recordo.read;

import com.cariochi.recordo.core.EnableRecordo;
import com.cariochi.recordo.core.RegularExtension;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.cariochi.reflecto.Reflecto.reflect;

@Deprecated(forRemoval = true)
public class ObjectFactoryFieldResolver implements RegularExtension, BeforeEachCallback {

    private final ObjectFactoryCreator objectFactoryCreator = new ObjectFactoryCreator();

    @Override
    public void beforeEach(ExtensionContext context) {
        reflect(context.getRequiredTestInstance()).includeEnclosing().fields().stream()
                .filter(field -> field.annotations().contains(EnableRecordo.class))
                .filter(field -> objectFactoryCreator.isSupported(field.type()))
                .forEach(field -> field.setValue(objectFactoryCreator.create(field.type(), context)));
    }

}
