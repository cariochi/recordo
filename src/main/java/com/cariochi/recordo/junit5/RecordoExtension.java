package com.cariochi.recordo.junit5;

import com.cariochi.recordo.handler.CompositeAnnotationHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.util.List;

import static java.util.Arrays.asList;

@Slf4j
public class RecordoExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final CompositeAnnotationHandler interceptor = new CompositeAnnotationHandler();

    private final List<ParamResolver> parameterResolvers = asList(
            new GivenParameterResolver(),
            new VerifyParameterResolver(),
            new RequestParameterResolver()
    );

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.beforeTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        context.getTestInstance().ifPresent(testInstance ->
                interceptor.afterTest(testInstance, context.getRequiredTestMethod())
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        return parameterResolvers.stream()
                .anyMatch(r -> r.supports(new RecordoContext(parameter, extension)));
    }

    @Override
    public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) throws ParameterResolutionException {
        final RecordoContext context = new RecordoContext(parameter, extension);
        return parameterResolvers.stream()
                .filter(r -> r.supports(context))
                .findFirst()
                .map(r -> r.resolveParameter(context))
                .orElse(null);
    }

}
