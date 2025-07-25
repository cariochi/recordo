package com.cariochi.recordo.read;

import com.cariochi.objecto.extension.ObjectoExtension;
import com.cariochi.recordo.core.RegularExtension;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ObjectoSeedResolver implements RegularExtension, BeforeEachCallback, AfterEachCallback {

    private final ObjectoExtension objectoExtension = new ObjectoExtension();

    @Override
    public void afterEach(ExtensionContext context) {
        objectoExtension.afterEach(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        objectoExtension.beforeEach(context);
    }
}
