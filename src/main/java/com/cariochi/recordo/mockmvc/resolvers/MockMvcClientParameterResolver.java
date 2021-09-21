package com.cariochi.recordo.mockmvc.resolvers;

import com.cariochi.recordo.mockmvc.RecordoMockMvc;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

public class MockMvcClientParameterResolver extends AbstractRequestParameterResolver {

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
