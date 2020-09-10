package com.cariochi.recordo;

import com.cariochi.recordo.given.GivenAnnotationHandler;
import com.cariochi.recordo.given.GivenParameterResolver;
import com.cariochi.recordo.mockhttp.HttpMocksAnnotationHandler;
import com.cariochi.recordo.mockhttp.MockHttpParameterResolver;
import com.cariochi.recordo.mockmvc.resolvers.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.util.List;

import static com.cariochi.recordo.utils.exceptions.Exceptions.tryAccept;
import static java.util.Arrays.asList;

@Slf4j
public class RecordoExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final List<Extension> handlers = asList(
            new GivenAnnotationHandler(),
            new HttpMocksAnnotationHandler()
    );

    private final List<ParameterResolver> parameterResolvers = asList(
            new GivenParameterResolver(),
            new GetParameterResolver(),
            new PostParameterResolver(),
            new PutParameterResolver(),
            new PatchParameterResolver(),
            new DeleteParameterResolver(),
            new MockHttpParameterResolver()
    );

    @Override
    public void beforeEach(ExtensionContext context) {
        handlers.stream()
                .filter(i -> BeforeEachCallback.class.isAssignableFrom(i.getClass()))
                .map(BeforeEachCallback.class::cast)
                .forEach(tryAccept(processor -> processor.beforeEach(context)));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        handlers.stream()
                .filter(i -> AfterEachCallback.class.isAssignableFrom(i.getClass()))
                .map(AfterEachCallback.class::cast)
                .forEach(tryAccept(processor -> processor.afterEach(context)));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameter,
                                     ExtensionContext extension) throws ParameterResolutionException {
        return parameterResolvers.stream()
                .anyMatch(r -> r.supportsParameter(parameter, extension));
    }

    @Override
    public Object resolveParameter(ParameterContext parameter,
                                   ExtensionContext extension) throws ParameterResolutionException {
        return parameterResolvers.stream()
                .filter(r -> r.supportsParameter(parameter, extension))
                .findFirst()
                .map(r -> r.resolveParameter(parameter, extension))
                .orElse(null);
    }

}
