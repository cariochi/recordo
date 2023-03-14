package com.cariochi.recordo.mockmvc.extensions;

import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import static com.cariochi.recordo.mockmvc.utils.MockMvcUtils.getMockMvcClient;

public class MockMvcExtension extends AbstractMockMvcExtension {

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return RecordoMockMvc.class.isAssignableFrom(parameter.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        return getMockMvcClient(extension);
    }
}
